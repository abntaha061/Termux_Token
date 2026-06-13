package com.example.pdfreader.data

import android.graphics.Bitmap
import com.example.pdfreader.native.ParsedHiddenLink

/**
 * يمثل رابطًا (Link Annotation) موجودًا على صفحة PDF،
 * مع موقعه (المستطيل المحيط به بالنسبة لحجم الصفحة) ونوعه بعد التحليل.
 */
data class PdfLinkAnnotation(
    val bounds: PdfRectF,       // موقع الرابط ضمن الصفحة (نسبي 0..1)
    val rawUri: String,         // الرابط/الأكشن الخام كما هو في الملف
    val parsed: ParsedHiddenLink // النتيجة بعد تحليله عبر المكتبة الأصلية C++
)

/** مستطيل بنسب من 0 إلى 1 بالنسبة لأبعاد الصفحة، لتسهيل رسمه فوق أي مقياس عرض */
data class PdfRectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/** حالة تحميل صفحة واحدة، تُستخدم للعرض التدريجي (lazy rendering) */
sealed class PdfPageState {
    object Loading : PdfPageState()
    data class Loaded(
        val bitmap: Bitmap,
        val widthPx: Int,
        val heightPx: Int,
        val links: List<PdfLinkAnnotation>
    ) : PdfPageState()
    data class Error(val message: String) : PdfPageState()
}

/** معلومات أساسية عن المستند بعد فتحه */
data class PdfDocumentInfo(
    val pageCount: Int,
    val title: String?,
    val fileName: String
)
