package com.example.ui.news

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.news.NewsArticleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

internal object NewsVoicePlaybackTracker {
    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    fun setSpeaking(speaking: Boolean) {
        _isSpeakingFlow.value = speaking
    }
}

private val DATE_STAMP_REGEX = Regex(
    """(?i)\b\d{4}-\d{1,2}-\d{1,2}(?:[T ]\d{1,2}:\d{2}(?::\d{2})?(?:\s*(?:Z|GMT|UTC|[+-]\d{2}:?\d{2}))?)?\b|\b\d{1,2}/\d{1,2}/\d{2,4}\b"""
)
private val TIME_METADATA_REGEX = Regex(
    """(?i)(?:[•|—–-]\s*)?(?:منذ\s+\d+\s*(?:ثانية|ثوانٍ|دقيقة|دقائق|ساعة|ساعات|يوم|أيام)|(?:published|updated)\s+(?:on|at)\s+[^•|\n]+|\b\d+\s+(?:sec|secs|second|seconds|min|mins|minute|minutes|hr|hrs|hour|hours|day|days)\s+ago\b)"""
)
private val MULTI_SPACE_REGEX = Regex("""\s+""")

/**
 * Builds clean text for the TTS engine from the currently displayed news article,
 * explicitly excluding [NewsArticleItem.timeAgo] and stripping embedded date/timestamp metadata.
 */
internal fun buildSpokenNewsText(story: NewsArticleItem): String {
    fun cleanForSpeech(raw: String): String {
        var text = raw
            .replace(DATE_STAMP_REGEX, " ")
            .replace(TIME_METADATA_REGEX, " ")
            .replace("•", ", ")
            .replace("|", ", ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
        // Remove dangling separators left behind after stripping timestamps
        text = text.trimEnd(',', '-', '—', '–', ':', ' ')
        return text
    }

    val headline = cleanForSpeech(story.headline)
    val summary = cleanForSpeech(story.summary)

    if (headline.isBlank()) return summary
    if (summary.isBlank()) return headline

    val normHeadline = headline.lowercase(Locale.ROOT)
    val normSummary = summary.lowercase(Locale.ROOT)
    if (normSummary.contains(normHeadline) || normHeadline.contains(normSummary)) {
        return headline
    }

    val separator = if (headline.endsWith(".") || headline.endsWith("!") || headline.endsWith("?") || headline.endsWith("؟")) {
        " "
    } else {
        ". "
    }
    return "$headline$separator$summary"
}

private data class PendingNewsSpeech(
    val storyKey: String,
    val text: String,
    val effectiveLang: String
)

@Stable
internal class NewsVoiceReader(context: Context) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    @Volatile
    private var isInitialized = false
    @Volatile
    private var isUnavailable = false
    @Volatile
    private var isShutDown = false

    @Volatile
    private var activeStoryKey: String? = null
    @Volatile
    private var completedStoryKeySync: String? = null
    private var pendingSpeech: PendingNewsSpeech? = null
    private var completedStoryKey by mutableStateOf<String?>(null)

    var isSpeaking by mutableStateOf(false)
        private set

    private fun markStoryCompleted(utteranceId: String?) {
        if (utteranceId == null || utteranceId != activeStoryKey) return
        completedStoryKeySync = utteranceId
        mainHandler.post {
            if (!isShutDown && utteranceId == activeStoryKey) {
                completedStoryKey = utteranceId
                isSpeaking = false
                NewsVoicePlaybackTracker.setSpeaking(false)
            }
        }
    }

    init {
        try {
            var engineRef: TextToSpeech? = null
            engineRef = TextToSpeech(appContext) { status ->
                if (isShutDown) {
                    try {
                        engineRef?.shutdown()
                    } catch (_: Exception) {}
                    return@TextToSpeech
                }
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    val engine = engineRef
                    try {
                        engine?.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                    } catch (_: Exception) {}

                    engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            if (utteranceId == activeStoryKey) {
                                mainHandler.post {
                                    if (!isShutDown && utteranceId == activeStoryKey) {
                                        isSpeaking = true
                                        NewsVoicePlaybackTracker.setSpeaking(true)
                                    }
                                }
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            markStoryCompleted(utteranceId)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            markStoryCompleted(utteranceId)
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            markStoryCompleted(utteranceId)
                        }

                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            markStoryCompleted(utteranceId)
                        }
                    })

                    val queued = pendingSpeech
                    pendingSpeech = null
                    if (queued != null && queued.storyKey == activeStoryKey) {
                        performSpeak(queued)
                    }
                } else {
                    isUnavailable = true
                    isSpeaking = false
                    NewsVoicePlaybackTracker.setSpeaking(false)
                    completedStoryKeySync = activeStoryKey
                    completedStoryKey = activeStoryKey
                }
            }
            tts = engineRef
        } catch (_: Exception) {
            isUnavailable = true
            isSpeaking = false
            NewsVoicePlaybackTracker.setSpeaking(false)
        }
    }

    fun speakStory(storyKey: String, text: String, effectiveLang: String) {
        if (isShutDown) return
        val cleanText = text.trim()
        activeStoryKey = storyKey
        if (cleanText.isBlank() || isUnavailable) {
            completedStoryKeySync = storyKey
            completedStoryKey = storyKey
            isSpeaking = false
            NewsVoicePlaybackTracker.setSpeaking(false)
            return
        }
        if (!isInitialized) {
            completedStoryKeySync = null
            completedStoryKey = null
            pendingSpeech = PendingNewsSpeech(storyKey, cleanText, effectiveLang)
            isSpeaking = true
            NewsVoicePlaybackTracker.setSpeaking(true)
            return
        }
        completedStoryKeySync = null
        completedStoryKey = null
        performSpeak(PendingNewsSpeech(storyKey, cleanText, effectiveLang))
    }

    private fun performSpeak(request: PendingNewsSpeech) {
        val engine = tts
        if (engine == null || isShutDown || isUnavailable) {
            completedStoryKeySync = request.storyKey
            completedStoryKey = request.storyKey
            isSpeaking = false
            NewsVoicePlaybackTracker.setSpeaking(false)
            return
        }
        try {
            val localeOk = applyLanguageLocale(engine, request.effectiveLang)
            if (!localeOk) {
                completedStoryKeySync = request.storyKey
                completedStoryKey = request.storyKey
                isSpeaking = false
                NewsVoicePlaybackTracker.setSpeaking(false)
                return
            }
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, request.storyKey)
            }
            isSpeaking = true
            NewsVoicePlaybackTracker.setSpeaking(true)
            val result = engine.speak(request.text, TextToSpeech.QUEUE_FLUSH, params, request.storyKey)
            if (result != TextToSpeech.SUCCESS) {
                completedStoryKeySync = request.storyKey
                completedStoryKey = request.storyKey
                isSpeaking = false
                NewsVoicePlaybackTracker.setSpeaking(false)
            }
        } catch (_: Exception) {
            completedStoryKeySync = request.storyKey
            completedStoryKey = request.storyKey
            isSpeaking = false
            NewsVoicePlaybackTracker.setSpeaking(false)
        }
    }

    fun hasCompletedStory(storyKey: String): Boolean {
        if (isUnavailable || isShutDown) return true
        if (activeStoryKey != storyKey) return true
        return completedStoryKeySync == storyKey || completedStoryKey == storyKey
    }

    fun stop() {
        pendingSpeech = null
        activeStoryKey?.let {
            completedStoryKeySync = it
            completedStoryKey = it
        }
        isSpeaking = false
        NewsVoicePlaybackTracker.setSpeaking(false)
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        isShutDown = true
        pendingSpeech = null
        mainHandler.removeCallbacksAndMessages(null)
        isSpeaking = false
        NewsVoicePlaybackTracker.setSpeaking(false)
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }

    private fun applyLanguageLocale(engine: TextToSpeech, effectiveLang: String): Boolean {
        val candidates = when (effectiveLang) {
            "ar" -> listOf("ar-SA", "ar-EG", "ar").map(Locale::forLanguageTag)
            "fr" -> listOf(Locale.FRANCE, Locale.FRENCH)
            "es" -> listOf("es-ES", "es-MX", "es").map(Locale::forLanguageTag)
            "it" -> listOf(Locale.ITALY, Locale.ITALIAN)
            "ru" -> listOf("ru-RU", "ru").map(Locale::forLanguageTag)
            "zh" -> listOf(Locale.SIMPLIFIED_CHINESE, Locale.CHINESE)
            "hi" -> listOf("hi-IN", "hi").map(Locale::forLanguageTag)
            "ko" -> listOf(Locale.KOREA, Locale.KOREAN)
            "ja" -> listOf(Locale.JAPAN, Locale.JAPANESE)
            "de" -> listOf(Locale.GERMANY, Locale.GERMAN)
            "pt" -> listOf("pt-BR", "pt-PT", "pt").map(Locale::forLanguageTag)
            "tr" -> listOf("tr-TR", "tr").map(Locale::forLanguageTag)
            "id", "in" -> listOf("id-ID", "id").map(Locale::forLanguageTag)
            "ur" -> listOf("ur-PK", "ur-IN", "ur").map(Locale::forLanguageTag)
            "fa" -> listOf("fa-IR", "fa").map(Locale::forLanguageTag)
            "pl" -> listOf("pl-PL", "pl").map(Locale::forLanguageTag)
            "nl" -> listOf("nl-NL", "nl").map(Locale::forLanguageTag)
            "vi" -> listOf("vi-VN", "vi").map(Locale::forLanguageTag)
            "th" -> listOf("th-TH", "th").map(Locale::forLanguageTag)
            "el" -> listOf("el-GR", "el").map(Locale::forLanguageTag)
            else -> listOf(Locale.US, Locale.UK, Locale.ENGLISH)
        }
        for (candidate in candidates) {
            val avail = try {
                engine.isLanguageAvailable(candidate)
            } catch (_: Exception) {
                TextToSpeech.LANG_NOT_SUPPORTED
            }
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                val res = engine.setLanguage(candidate)
                if (res >= TextToSpeech.LANG_AVAILABLE) {
                    return true
                }
            }
        }
        return false
    }
}

@Composable
internal fun rememberNewsVoiceReader(): NewsVoiceReader {
    val context = androidx.compose.ui.platform.LocalContext.current
    val reader = remember(context) { NewsVoiceReader(context) }
    DisposableEffect(reader) {
        onDispose {
            reader.shutdown()
        }
    }
    return reader
}
