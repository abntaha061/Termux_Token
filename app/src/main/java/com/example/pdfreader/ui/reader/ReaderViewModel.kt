package com.example.pdfreader.ui.reader

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfreader.data.PdfDocumentInfo
import com.example.pdfreader.data.PdfPageState
import com.example.pdfreader.data.PdfRepository
import com.example.pdfreader.data.TtsManager
import com.example.pdfreader.native.HiddenLinkType
import com.example.pdfreader.native.NativeLib
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** حالة الواجهة الكاملة لشاشة القراءة */
data class ReaderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val docInfo: PdfDocumentInfo? = null,
    val currentPage: Int = 0,
    val isToolbarVisible: Boolean = true,
    val isSpeaking: Boolean = false,
    val selectedText: String? = null
)

/**
 * يدير حالة فتح المستند، الصفحة الحالية، ظهور/اختفاء شريط الأدوات،
 * والتفاعل مع الروابط المخفية (نطق) والتحديد النصي.
 */
class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PdfRepository(application)
    private val tts = TtsManager(application)

    // ذاكرة تخزين مؤقت بسيطة لحالات الصفحات المُحمّلة (lazy loading)
    private val pageCache = mutableMapOf<Int, PdfPageState>()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _pageStates = MutableStateFlow<Map<Int, PdfPageState>>(emptyMap())
    val pageStates: StateFlow<Map<Int, PdfPageState>> = _pageStates.asStateFlow()

    fun openDocument(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            pageCache.clear()
            _pageStates.value = emptyMap()

            try {
                val info = repository.open(uri)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    docInfo = info,
                    currentPage = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "تعذر فتح الملف"
                )
            }
        }
    }

    /** يطلب عرض صفحة معينة بدقة معينة، ويخزّنها مؤقتًا */
    fun requestPage(pageIndex: Int, widthPx: Int, heightPx: Int) {
        if (pageCache.containsKey(pageIndex)) return
        if (widthPx <= 0 || heightPx <= 0) return

        pageCache[pageIndex] = PdfPageState.Loading
        _pageStates.value = pageCache.toMap()

        viewModelScope.launch {
            val state = repository.renderPage(pageIndex, widthPx, heightPx)
            pageCache[pageIndex] = state
            _pageStates.value = pageCache.toMap()
        }
    }

    fun onPageChanged(pageIndex: Int) {
        _uiState.value = _uiState.value.copy(currentPage = pageIndex)
    }

    fun toggleToolbar() {
        _uiState.value = _uiState.value.copy(isToolbarVisible = !_uiState.value.isToolbarVisible)
    }

    fun setToolbarVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isToolbarVisible = visible)
    }

    /**
     * يُستدعى عند الضغط على رابط مخفي.
     * - إن كان نطق كلمة/جملة: يشغّل TTS مباشرة.
     * - إن كان مرجعًا داخليًا: يُعاد فهرس الصفحة المطلوبة (يُعالج في الواجهة).
     * - إن كان رابطًا خارجيًا: يُعاد عبر callback للواجهة لفتحه بمتصفح.
     */
    fun onLinkClicked(type: HiddenLinkType, payload: String, langHint: String) {
        when (type) {
            HiddenLinkType.TTS_WORD, HiddenLinkType.TTS_SENTENCE -> speak(payload, langHint)
            else -> { /* تُعالج في الواجهة (فتح رابط خارجي / الانتقال لصفحة) */ }
        }
    }

    fun speak(text: String, langHint: String? = null) {
        if (text.isBlank()) return
        tts.speak(text, langHint)
        _uiState.value = _uiState.value.copy(isSpeaking = true, selectedText = text)
    }

    fun stopSpeaking() {
        tts.stop()
        _uiState.value = _uiState.value.copy(isSpeaking = false)
    }

    fun onTextSelected(text: String?) {
        _uiState.value = _uiState.value.copy(selectedText = text)
    }

    /** يستخرج كلمات نص الصفحة عبر مكتبة C++ (تُستخدم لتمييز/نطق كلمة بمفردها) */
    fun splitWords(text: String): List<String> {
        return NativeLib.splitIntoWords(text).toList()
    }

    fun cleanWord(word: String): String = NativeLib.cleanWord(word)

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
        repository.close()
    }
}
