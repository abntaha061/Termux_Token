package com.example.pdfreader.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.pdfreader.data.PdfLinkAnnotation
import com.example.pdfreader.data.PdfPageState
import com.example.pdfreader.native.HiddenLinkType

/**
 * يعرض صفحة واحدة من المستند:
 *  - تُحمَّل بدقة عالية تتناسب مع عرض الشاشة الفعلي × معامل تكبير (لجودة عند الزووم)
 *  - تُعرض الروابط (المخفية وغيرها) كمناطق قابلة للضغط فوق الصورة
 *  - تُظهر مؤشر تحميل أثناء العرض (rendering تدريجي/lazy)
 */
@Composable
fun PdfPageView(
    pageIndex: Int,
    pageState: PdfPageState?,
    aspectRatio: Float,
    renderScale: Float,
    onRequestRender: (widthPx: Int, heightPx: Int) -> Unit,
    onLinkClick: (PdfLinkAnnotation) -> Unit,
    modifier: Modifier = Modifier
) {
    var sizePx by remember { mutableStateOf(0 to 0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(4.dp))
            .onSizeChanged { size ->
                sizePx = size.width to size.height
            }
    ) {
        // طلب رسم الصفحة بدقة (العرض الظاهر × معامل التكبير) للحفاظ على وضوح عالٍ عند الزووم
        LaunchedEffect(sizePx, renderScale) {
            val (w, h) = sizePx
            if (w > 0 && h > 0) {
                val targetW = (w * renderScale).toInt().coerceAtMost(2480) // حد أعلى يقارب A4 عند 300dpi
                val targetH = (h * renderScale).toInt().coerceAtMost(3508)
                onRequestRender(targetW, targetH)
            }
        }

        when (pageState) {
            null, is PdfPageState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is PdfPageState.Error -> {
                Text(
                    text = "تعذر عرض الصفحة ${pageIndex + 1}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }

            is PdfPageState.Loaded -> {
                Image(
                    bitmap = pageState.bitmap.asImageBitmap(),
                    contentDescription = "صفحة ${pageIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // طبقة الروابط الشفافة فوق الصورة
                LinkOverlay(
                    links = pageState.links,
                    onLinkClick = onLinkClick
                )
            }
        }
    }
}

/** يرسم مناطق قابلة للنقر فوق الروابط (المخفية والعادية) بنسب موضعها */
@Composable
private fun LinkOverlay(
    links: List<PdfLinkAnnotation>,
    onLinkClick: (PdfLinkAnnotation) -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        links.forEach { link ->
            val leftPx = link.bounds.left * widthPx
            val topPx = link.bounds.top * heightPx
            val rightPx = link.bounds.right * widthPx
            val bottomPx = link.bounds.bottom * heightPx

            val widthDp = with(density) { (rightPx - leftPx).coerceAtLeast(1f).toDp() }
            val heightDp = with(density) { (bottomPx - topPx).coerceAtLeast(1f).toDp() }
            val leftDp = with(density) { leftPx.toDp() }
            val topDp = with(density) { topPx.toDp() }

            val isHiddenLearningLink = link.parsed.type == HiddenLinkType.TTS_WORD ||
                    link.parsed.type == HiddenLinkType.TTS_SENTENCE

            var boxModifier = Modifier
                .offset(x = leftDp, y = topDp)
                .size(width = widthDp, height = heightDp)

            if (isHiddenLearningLink) {
                boxModifier = boxModifier.background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(3.dp)
                )
            }

            Box(
                modifier = boxModifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onLinkClick(link)
                }
            )
        }
    }
}
