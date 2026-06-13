package com.example.pdfreader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * شريط علوي شفاف (عنوان الملف + رجوع + مشاركة + إشارة مرجعية).
 * يظهر/يختفي مع شريط الأدوات السفلي بحركة انتقالية ناعمة.
 */
@Composable
fun ReaderTopBar(
    visible: Boolean,
    title: String,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            )
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "إشارة مرجعية"
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "مشاركة")
            }
        }
    }
}

/**
 * شريط الأدوات السفلي: يحتوي على
 *  - رقم الصفحة الحالية / الإجمالي
 *  - تكبير/تصغير
 *  - بحث
 *  - فهرس/قائمة الصفحات
 * يظهر/يختفي عند ضغط المستخدم في أي مكان على الشاشة.
 */
@Composable
fun ReaderBottomBar(
    visible: Boolean,
    currentPage: Int,
    pageCount: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onSearch: () -> Unit,
    onShowOutline: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Column {
            // مؤشر الصفحة الحالية
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentPage + 1} / $pageCount",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarAction(icon = Icons.Filled.List, label = "الفهرس", onClick = onShowOutline)
                ToolbarAction(icon = Icons.Filled.ZoomOut, label = "تصغير", onClick = onZoomOut)
                ToolbarAction(icon = Icons.Filled.ZoomIn, label = "تكبير", onClick = onZoomIn)
                ToolbarAction(icon = Icons.Filled.Search, label = "بحث", onClick = onSearch)
            }
        }
    }
}

@Composable
private fun ToolbarAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
    }
}
