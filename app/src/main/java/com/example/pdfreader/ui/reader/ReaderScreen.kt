package com.example.pdfreader.ui.reader

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfreader.data.PdfLinkAnnotation
import com.example.pdfreader.native.HiddenLinkType
import kotlin.math.max

/**
 * الشاشة الرئيسية لعرض ملف PDF:
 *  - عرض ملء الشاشة عند الفتح (يُدار من MainActivity عبر إخفاء أشرطة النظام)
 *  - تمرير عمودي مستمر بين الصفحات (LazyColumn) مع تحميل تدريجي للصفحات القريبة فقط
 *  - دعم القرص (Pinch-to-Zoom) والتمرير عند التكبير
 *  - الضغط في أي مكان يُظهر/يخفي شريط الأدوات العلوي والسفلي
 *  - الضغط على رابط مخفي (نطق) يشغّل الصوت داخل التطبيق
 */
@Composable
fun ReaderScreen(
    fileUri: Uri,
    fileName: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pageStates by viewModel.pageStates.collectAsState()

    var isBookmarked by remember { mutableStateOf(false) }

    // حالة التكبير/التحريك العامة (Pinch zoom)
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val listState = rememberLazyListState()

    // فتح الملف عند أول دخول للشاشة
    LaunchedEffect(fileUri) {
        viewModel.openDocument(fileUri)
    }

    // تتبع الصفحة الحالية بناءً على أول عنصر مرئي
    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.onPageChanged(listState.firstVisibleItemIndex)
    }

    val pageCount = uiState.docInfo?.pageCount ?: 0

    Scaffold(
        topBar = {
            ReaderTopBar(
                visible = uiState.isToolbarVisible,
                title = uiState.docInfo?.title?.takeIf { it.isNotBlank() } ?: fileName,
                isBookmarked = isBookmarked,
                onBack = onBack,
                onToggleBookmark = { isBookmarked = !isBookmarked },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة الملف"))
                }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                visible = uiState.isToolbarVisible,
                currentPage = uiState.currentPage,
                pageCount = pageCount,
                onZoomIn = {
                    scale = (scale * 1.25f).coerceIn(1f, 5f)
                },
                onZoomOut = {
                    scale = (scale / 1.25f).coerceIn(1f, 5f)
                    if (scale <= 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
                onSearch = { /* يمكن ربطها بشاشة بحث منفصلة */ },
                onShowOutline = { /* يمكن ربطها بقائمة فهرس/صفحات مصغّرة */ }
            )
        }
    ) { padding ->

        when {
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "حدث خطأ",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.isLoading || uiState.docInfo == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "جاري التحميل…", style = MaterialTheme.typography.bodyLarge)
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pointerInput(Unit) {
                            // الضغط في أي مكان يُظهر/يخفي شريط الأدوات
                            detectTapGestures(
                                onTap = { viewModel.toggleToolbar() }
                            )
                        }
                        .pointerInput(Unit) {
                            // دعم القرص للتكبير/التصغير والتحريك
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale
                                if (newScale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(count = pageCount, key = { it }) { index ->
                            val pageState = pageStates[index]

                            // معامل العرض/الارتفاع الافتراضي A4 (مناسب لأغلب كتب تعلم اللغات)
                            val aspect = 1f / 1.4142f

                            PdfPageView(
                                pageIndex = index,
                                pageState = pageState,
                                aspectRatio = aspect,
                                renderScale = max(2f, scale * 2f), // دقة أعلى عند التكبير لجودة حادة
                                onRequestRender = { w, h ->
                                    viewModel.requestPage(index, w, h)
                                },
                                onLinkClick = { link ->
                                    handleLinkClick(link, context, viewModel)
                                }
                            )
                        }
                    }

                    // مؤشر حالة النطق الحالي
                    if (uiState.isSpeaking) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 96.dp)
                                .padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                text = "🔊 جارٍ النطق",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * يعالج الضغط على رابط (مخفي أو عادي):
 *  - TTS_WORD / TTS_SENTENCE: نطق داخل التطبيق عبر ViewModel
 *  - EXTERNAL_URL: فتح في المتصفح الافتراضي
 *  - INTERNAL_REF / NONE: غير مُفعّل حاليًا (قابل للتوسعة لاحقًا)
 */
private fun handleLinkClick(
    link: PdfLinkAnnotation,
    context: android.content.Context,
    viewModel: ReaderViewModel
) {
    when (link.parsed.type) {
        HiddenLinkType.TTS_WORD, HiddenLinkType.TTS_SENTENCE -> {
            viewModel.speak(link.parsed.payload, link.parsed.langHint.ifBlank { null })
        }
        HiddenLinkType.EXTERNAL_URL -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.parsed.payload))
                context.startActivity(intent)
            } catch (e: Exception) {
                // تعذر فتح الرابط - يمكن عرض رسالة للمستخدم
            }
        }
        HiddenLinkType.INTERNAL_REF, HiddenLinkType.NONE -> {
            // يمكن تنفيذ الانتقال للصفحة المطلوبة لاحقًا
        }
    }
}
