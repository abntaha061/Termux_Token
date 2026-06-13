package com.example.pdfreader.ui.theme

import androidx.compose.ui.graphics.Color

// ====== لوحة الألوان الأساسية للتطبيق ======
// لون أساسي (Primary): أزرق هادئ يناسب القراءة الطويلة
val PrimaryLight = Color(0xFF2563EB)   // أزرق متوسط
val PrimaryDark  = Color(0xFF90B4FE)   // أزرق فاتح للوضع الداكن

// لون ثانوي/تمييز (Accent): يستخدم لتمييز الروابط المخفية والكلمات القابلة للنطق
val AccentLight = Color(0xFFF59E0B)    // برتقالي ذهبي
val AccentDark  = Color(0xFFFFC56D)

// خلفيات
val BackgroundLight = Color(0xFFF7F7F5) // خلفية فاتحة قريبة من ورق الكتاب
val BackgroundDark  = Color(0xFF121212) // خلفية داكنة كلاسيكية لقراء PDF

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark  = Color(0xFF1E1E1E)

// شريط الأدوات السفلي
val ToolbarLight = Color(0xE6FFFFFF) // شبه شفاف
val ToolbarDark  = Color(0xE6242424)

// تمييز النص المحدد
val SelectionHighlight = Color(0x552563EB)

// تمييز الكلمة عند نطقها (Word highlight during TTS)
val SpeakingHighlight = Color(0x66F59E0B)

val OnPrimaryLight = Color(0xFFFFFFFF)
val OnPrimaryDark  = Color(0xFF003062)

val ErrorColor = Color(0xFFBA1A1A)
