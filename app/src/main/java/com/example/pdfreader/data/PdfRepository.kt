package com.example.pdfreader.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.example.pdfreader.native.NativeLib
import io.legere.pdfiumandroid.PdfDocument
import io.legere.pdfiumandroid.PdfiumCore
import io.legere.pdfiumandroid.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * يغلّف مكتبة PDFium (pdfium-android، بنية تحتية C/C++ من PDFium الخاصة بـ Chromium)
 * لتوفير:
 *   - معلومات المستند (عدد الصفحات، العنوان)
 *   - عرض الصفحات كصور (Bitmap) بدقة عالية وقابلة للتكبير
 *   - استخراج روابط/تعليقات (Link Annotations) كل صفحة وتمريرها
 *     إلى مكتبة C++ الخاصة بنا (NativeLib) لتحليل الروابط المخفية لتعلم اللغات.
 *   - استخراج النص الكامل لكل صفحة (لدعم التحديد والنطق).
 */
class PdfRepository(private val context: Context) {

    private val core = PdfiumCore(context)
    private var document: PdfDocument? = null
    private var docInfo: PdfDocumentInfo? = null

    /** يفتح ملف PDF من Uri (يدعم content:// و file://) */
    suspend fun open(uri: Uri): PdfDocumentInfo = withContext(Dispatchers.IO) {
        close()

        val file = copyUriToCache(uri)
        val doc = core.newDocument(file)
        document = doc

        val pageCount = doc.getPageCount()
        val title = doc.getDocumentMeta().title?.takeIf { it.isNotBlank() }

        val info = PdfDocumentInfo(
            pageCount = pageCount,
            title = title,
            fileName = file.name
        )
        docInfo = info
        info
    }

    /**
     * يرسم صفحة كـ Bitmap بدقة (widthPx x heightPx) ويستخرج روابطها.
     * يُستحسن استدعاء هذا من Dispatchers.Default/IO لأنه عملية ثقيلة.
     */
    suspend fun renderPage(pageIndex: Int, widthPx: Int, heightPx: Int): PdfPageState =
        withContext(Dispatchers.Default) {
            val doc = document ?: return@withContext PdfPageState.Error("المستند غير مفتوح")

            try {
                val page = doc.openPage(pageIndex)
                try {
                    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)

                    page.renderPageBitmap(
                        bitmap,
                        startX = 0,
                        startY = 0,
                        drawSizeX = widthPx,
                        drawSizeY = heightPx,
                        renderAnnot = true
                    )

                    val links = extractLinks(page, widthPx, heightPx)

                    PdfPageState.Loaded(
                        bitmap = bitmap,
                        widthPx = widthPx,
                        heightPx = heightPx,
                        links = links
                    )
                } finally {
                    page.close()
                }
            } catch (e: Exception) {
                PdfPageState.Error(e.message ?: "خطأ غير معروف")
            }
        }

    /** يستخرج النص الكامل لصفحة معينة (يُستخدم للنطق والبحث) */
    suspend fun getPageText(pageIndex: Int): String = withContext(Dispatchers.Default) {
        val doc = document ?: return@withContext ""
        val page = doc.openPage(pageIndex)
        try {
            val textPage = page.openTextPage()
            try {
                val charCount = textPage.textPageCountChars()
                if (charCount <= 0) "" else textPage.textPageGetText(0, charCount) ?: ""
            } finally {
                textPage.close()
            }
        } catch (e: Exception) {
            ""
        } finally {
            page.close()
        }
    }

    /** يحصل على الأبعاد الأصلية للصفحة (بالنقاط PDF) للحفاظ على نسبة العرض/الارتفاع */
    suspend fun getPageSize(pageIndex: Int): Size = withContext(Dispatchers.Default) {
        val doc = document ?: return@withContext Size(595, 842) // افتراضي A4
        val page = doc.openPage(pageIndex)
        try {
            Size(page.getPageWidthPoint(), page.getPageHeightPoint())
        } finally {
            page.close()
        }
    }

    /**
     * يستخرج الروابط (Link Annotations) من الصفحة عبر PDFium،
     * ثم يمرر كل رابط إلى NativeLib.parseHiddenLink لتحديد إن كان
     * رابط نطق مخفي خاص بتعلم اللغات أم رابطًا عاديًا.
     */
    private fun extractLinks(page: PdfDocument.Page, widthPx: Int, heightPx: Int): List<PdfLinkAnnotation> {
        return try {
            val links = page.getPageLinks()
            val pageW = page.getPageWidthPoint().toFloat()
            val pageH = page.getPageHeightPoint().toFloat()
            val result = mutableListOf<PdfLinkAnnotation>()

            for (link in links) {
                val uri = link.uri
                val rect = link.bounds ?: continue
                if (uri.isNullOrBlank()) continue

                val parsed = NativeLib.parseHiddenLink(uri)

                result.add(
                    PdfLinkAnnotation(
                        bounds = PdfRectF(
                            left = (rect.left / pageW).coerceIn(0f, 1f),
                            top = (1f - rect.top / pageH).coerceIn(0f, 1f),
                            right = (rect.right / pageW).coerceIn(0f, 1f),
                            bottom = (1f - rect.bottom / pageH).coerceIn(0f, 1f)
                        ),
                        rawUri = uri,
                        parsed = com.example.pdfreader.native.ParsedHiddenLink.from(parsed)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun close() {
        document?.close()
        document = null
        docInfo = null
    }

    /** ينسخ ملف الـ Uri إلى مساحة تخزين مؤقتة كي تتمكن PDFium من فتحه عبر مسار ملف عادي */
    private fun copyUriToCache(uri: Uri): File {
        val outFile = File(context.cacheDir, "current_document.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("تعذر فتح الملف المحدد")
        return outFile
    }
}
