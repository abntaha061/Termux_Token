package com.example.pdfreader.native

/**
 * واجهة Kotlin لمكتبة C++ الأصلية (pdfreader_native).
 * تُستخدم لتحليل الروابط المخفية الخاصة بتعلم اللغات (نطق الكلمات/الجمل)
 * ومعالجة النصوص المستخرجة من الصفحات.
 */
object NativeLib {

    init {
        System.loadLibrary("pdfreader_native")
    }

    /**
     * يحلل سلسلة Action/URI القادمة من رابط PDF (annotation).
     * يعيد مصفوفة من 3 عناصر: [type, payload, langHint]
     * type يكون أحد: NONE, TTS_WORD, TTS_SENTENCE, EXTERNAL_URL, INTERNAL_REF
     */
    external fun parseHiddenLink(rawUri: String): Array<String>

    /**
     * يستخرج جميع الروابط/الأنماط الخاصة بالنطق من نص صفحة كامل.
     * يعيد مصفوفة من مصفوفات [type, payload, langHint]
     */
    external fun extractLinksFromText(pageText: String): Array<Array<String>>

    /** ينظف كلمة من علامات الترقيم المحيطة بها */
    external fun cleanWord(word: String): String

    /** يتحقق إن كان النص يحتوي على حروف عربية */
    external fun containsArabic(text: String): Boolean

    /** يقسم نصًا إلى كلمات (يدعم Unicode/العربية) */
    external fun splitIntoWords(text: String): Array<String>
}

/** تمثيل Kotlin مريح لنتيجة [NativeLib.parseHiddenLink] */
data class ParsedHiddenLink(
    val type: HiddenLinkType,
    val payload: String,
    val langHint: String
) {
    companion object {
        fun from(arr: Array<String>): ParsedHiddenLink {
            val type = HiddenLinkType.entries.firstOrNull { it.name == arr[0] } ?: HiddenLinkType.NONE
            return ParsedHiddenLink(type, arr.getOrElse(1) { "" }, arr.getOrElse(2) { "" })
        }
    }
}

enum class HiddenLinkType {
    NONE, TTS_WORD, TTS_SENTENCE, EXTERNAL_URL, INTERNAL_REF
}
