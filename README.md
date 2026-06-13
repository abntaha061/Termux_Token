# قارئ PDF لتعلم اللغات (PDF Reader)

تطبيق Android لعرض ملفات PDF بجودة عالية (حتى 200 صفحة)، مكتوب بـ **Kotlin (Jetpack Compose)** مع طبقة **C++ (JNI/CMake)**، ويدعم:

- عرض ملء الشاشة عند فتح الملف.
- تمرير عمودي مستمر بين الصفحات مع تحميل تدريجي (lazy rendering) للحفاظ على الأداء مع ملفات كبيرة.
- تكبير/تصغير (Pinch-to-Zoom) مع إعادة الرسم بدقة أعلى عند التكبير لجودة حادة.
- شريط أدوات علوي وسفلي يظهر/يختفي عند الضغط على الشاشة.
- اكتشاف **الروابط المخفية** الخاصة بتعلم اللغات (مثل `tts://word`, `tts-fr://bonjour`, `##say:...##`) ونطقها داخل التطبيق عبر `TextToSpeech`.
- فتح الروابط الخارجية العادية (http/https) في المتصفح.
- ثيم Light/Dark تلقائي يتبع نظام الجهاز (مع دعم Material You على أندرويد 12+).

## البنية

```
app/
 ├─ src/main/cpp/              # مكتبة C++ (تحليل الروابط المخفية ومعالجة النصوص)
 │   ├─ link_parser.h / .cpp
 │   ├─ text_utils.h / .cpp
 │   ├─ native_bridge.cpp      # جسر JNI
 │   └─ CMakeLists.txt
 ├─ src/main/java/com/example/pdfreader/
 │   ├─ MainActivity.kt
 │   ├─ PdfReaderApp.kt
 │   ├─ native/NativeLib.kt    # واجهة Kotlin لمكتبة C++
 │   ├─ data/                  # PdfRepository (PDFium) + TtsManager + النماذج
 │   └─ ui/                    # شاشات Compose (Home + Reader + Theme)
 └─ src/main/res/
```

## مكتبة PDF المستخدمة

يستخدم التطبيق **PDFium** عبر مكتبة `io.legere:pdfiumandroid` (طبقة Kotlin/JNI فوق محرك PDFium الأصلي بلغة C/C++ التابع لمشروع Chromium)، وهي مسؤولة عن:
- فتح المستند وعرض الصفحات كـ Bitmap بدقة عالية.
- استخراج روابط (Link Annotations) كل صفحة.
- استخراج النص الكامل لكل صفحة.

أما مكتبة `pdfreader_native` (C++ خاصة بالمشروع) فهي مسؤولة عن:
- تحليل الروابط المخفية الخاصة بتعلم اللغات وتصنيفها (نطق كلمة/جملة، رابط خارجي، مرجع داخلي).
- معالجة النصوص (تقسيم كلمات، تنظيف علامات الترقيم، اكتشاف العربية).

## أنماط الروابط المخفية المدعومة

| النمط | الوصف |
|---|---|
| `tts://word` | نطق كلمة واحدة (اللغة تُحدَّد تلقائيًا) |
| `tts-en://hello` | نطق كلمة بلغة محددة (en, fr, de, ...) |
| `tts:word` | اختصار بدون `//` |
| `##tts:word##` | نمط مضمّن داخل نص الصفحة |
| `##say:a full sentence##` | نطق جملة كاملة |
| `http(s)://...` | رابط خارجي عادي يُفتح في المتصفح |
| `#page=12` أو `#12` | مرجع داخلي لصفحة (قابل للتوسعة) |

## البناء (Build)

1. افتح المشروع في **Android Studio** (إصدار حديث يدعم AGP 8.5 و NDK).
2. سيقوم Gradle بتنزيل تبعية `pdfiumandroid` من Maven/JitPack تلقائيًا.
3. تأكد من تثبيت **NDK** و **CMake** عبر SDK Manager (لبناء مكتبة `pdfreader_native`).
4. اضغط Run على جهاز/محاكي Android (API 24+).

## الرفع إلى GitHub

```bash
cd pdfreader
git init
git add .
git commit -m "Initial commit: PDF reader app with hidden link TTS support"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO_NAME.git
git push -u origin main
```

## ملاحظات وتوسعات مستقبلية

- يمكن إضافة شاشة بحث نصي كامل عبر `PdfRepository.getPageText`.
- يمكن إضافة قائمة فهرس/إشارات مرجعية محفوظة (DataStore/Room).
- يمكن دعم تحديد نص حقيقي (Text Selection) عبر PDFium TextPage API لإضافة قائمة نسخ/نطق/ترجمة عند التحديد.
