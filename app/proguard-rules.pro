# Keep PDFium JNI classes
-keep class io.legere.pdfiumandroid.** { *; }
-keep class com.example.pdfreader.native.** { *; }
-keepclassmembers class * {
    native <methods>;
}
