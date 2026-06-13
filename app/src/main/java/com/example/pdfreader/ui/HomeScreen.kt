package com.example.pdfreader.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * شاشة البداية: تسمح للمستخدم باختيار ملف PDF من الجهاز.
 * عند الاختيار، تُفتح شاشة القراءة بملء الشاشة مباشرة.
 */
@Composable
fun HomeScreen(onFileSelected: (Uri, String) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onFileSelected(uri, uri.lastPathSegment ?: "document.pdf")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "قارئ PDF لتعلم اللغات",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "اختر ملف PDF لعرضه بجودة عالية مع دعم الروابط الصوتية المخفية",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = { launcher.launch(arrayOf("application/pdf")) }) {
                Text("فتح ملف PDF")
            }
        }
    }
}
