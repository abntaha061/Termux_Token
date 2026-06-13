package com.example.pdfreader.data

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * يغلّف android.speech.tts.TextToSpeech لتشغيل النطق عند الضغط
 * على الروابط المخفية (TTS_WORD / TTS_SENTENCE) أو عند اختيار "نطق"
 * من القائمة المنبثقة لتحديد النص.
 */
class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
    }

    /**
     * ينطق النص المعطى. إن تم تمرير langHint (مثل "en"، "fr"، "de")
     * يُستخدم لتعيين لغة النطق، وإلا يتم تحديد اللغة تلقائيًا بناءً
     * على وجود حروف عربية في النص (عبر NativeLib.containsArabic).
     */
    fun speak(text: String, langHint: String? = null) {
        val engine = tts ?: return
        if (!ready || text.isBlank()) return

        val locale = when {
            !langHint.isNullOrBlank() -> Locale(langHint)
            com.example.pdfreader.native.NativeLib.containsArabic(text) -> Locale("ar")
            else -> Locale.getDefault()
        }

        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // الرجوع إلى الإنجليزية كخيار افتراضي مدعوم على أغلب الأجهزة
            engine.setLanguage(Locale.US)
        }

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pdf_tts_utterance")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
