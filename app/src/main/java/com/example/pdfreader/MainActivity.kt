package com.example.pdfreader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pdfreader.ui.HomeScreen
import com.example.pdfreader.ui.reader.ReaderScreen
import com.example.pdfreader.ui.theme.PdfReaderTheme

/**
 * النشاط الرئيسي: يتيح التنقل بين شاشة اختيار الملف (Home)
 * وشاشة القراءة بملء الشاشة (Reader)، مع إخفاء أشرطة النظام
 * عند فتح ملف لتجربة قراءة غامرة بملء الشاشة.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // إذا تم فتح التطبيق من تطبيق آخر بملف PDF (مثل مدير الملفات)
        val initialUri: Uri? = intent?.data

        setContent {
            PdfReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var openedFile by remember {
                        mutableStateOf<Pair<Uri, String>?>(
                            initialUri?.let { it to (it.lastPathSegment ?: "document.pdf") }
                        )
                    }

                    val current = openedFile
                    if (current == null) {
                        HomeScreen(onFileSelected = { uri, name -> openedFile = uri to name })
                    } else {
                        ReaderScreen(
                            fileUri = current.first,
                            fileName = current.second,
                            onBack = { openedFile = null }
                        )
                    }
                }
            }
        }
    }
}
