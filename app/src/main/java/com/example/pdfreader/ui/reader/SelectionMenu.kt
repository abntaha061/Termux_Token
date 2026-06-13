package com.example.pdfreader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/**
 * قائمة منبثقة تظهر عند تحديد المستخدم لنص في الصفحة، بها:
 *  - نسخ النص المحدد
 *  - نطق النص المحدد (TTS)
 *  - ترجمة (يفتح تطبيق/متصفح ترجمة خارجي عبر intent في الواجهة)
 *
 * تظهر بجانب نقطة التحديد (anchorOffset) وتختفي عند الضغط في أي مكان آخر.
 */
@Composable
fun SelectionMenu(
    anchorOffset: IntOffset,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onTranslate: () -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                // نضع القائمة فوق نقطة التحديد مع تصحيح إن خرجت عن حدود الشاشة
                var x = anchorOffset.x - popupContentSize.width / 2
                var y = anchorOffset.y - popupContentSize.height - 16

                x = x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
                y = y.coerceAtLeast(0)

                return IntOffset(x, y)
            }
        },
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ")
                }
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "نطق")
                }
                IconButton(onClick = onTranslate) {
                    Icon(Icons.Filled.Translate, contentDescription = "ترجمة")
                }
            }
        }
    }
}
