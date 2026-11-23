package com.example.data.news

import android.content.Context
import com.example.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NewsSectionType(val id: String) {
    TOP_STORIES("top_stories"),
    POLITICS("politics"),
    ECONOMY("economy"),
    TECHNOLOGY("technology"),
    WORLD("world")
}

data class NewsArticleItem(
    val id: String,
    val section: NewsSectionType,
    val categoryLabel: String,
    val headline: String,
    val summary: String,
    val sourceName: String,
    val timeAgo: String,
    val imageUrl: String = "",
    val visualTheme: String = "trading_floor"
)

data class LanguageNewsSourceConfig(
    val langCode: String,
    val primarySourceName: String,
    val secondarySourceName: String,
    val sectionFeeds: Map<NewsSectionType, List<String>>
)

data class NewsCenterState(
    val languageCode: String,
    val activeSourceName: String,
    val articlesBySection: Map<NewsSectionType, List<NewsArticleItem>>,
    val breakingTickerItems: List<String>,
    val updatedTime: String,
    val fetchedAtMillis: Long,
    val isLoading: Boolean = false
)

data class NewsLocalizedStrings(
    val newsCenterTitle: String,
    val liveBadge: String,
    val sectionsHeader: String,
    val sectionTopStories: String,
    val sectionPolitics: String,
    val sectionEconomy: String,
    val sectionTechnology: String,
    val sectionWorld: String,
    val upNextHeader: String,
    val upNextPrefix: String,
    val updatedPrefix: String,
    val breakingBadge: String,
    val sourceLabel: String,
    val refreshAction: String,
    val backToHomeHint: String
)

object NewsRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fetchMutex = Mutex()

    // High-resolution pure-JPEG editorial photo fallbacks (fm=jpg ensures 100% compatibility on all Android TV decoders)
    private val SECTION_FALLBACK_PHOTOS: Map<NewsSectionType, List<String>> = mapOf(
        NewsSectionType.TOP_STORIES to listOf(
            "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=960&q=80&fm=jpg&fit=crop"
        ),
        NewsSectionType.POLITICS to listOf(
            "https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1555848962-6e79363ec58f?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1577415124269-fc1140a69e91?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1523995462485-3d171b5c8fa9?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=960&q=80&fm=jpg&fit=crop"
        ),
        NewsSectionType.ECONOMY to listOf(
            "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1535320903710-d993d3d77d29?w=960&q=80&fm=jpg&fit=crop"
        ),
        NewsSectionType.TECHNOLOGY to listOf(
            "https://images.unsplash.com/photo-1518770660439-4636190af475?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=960&q=80&fm=jpg&fit=crop"
        ),
        NewsSectionType.WORLD to listOf(
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1444723121867-7a241cacace9?w=960&q=80&fm=jpg&fit=crop",
            "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=960&q=80&fm=jpg&fit=crop"
        )
    )

    private val SECONDARY_FALLBACK_PHOTOS: List<String> = listOf(
        "https://images.pexels.com/photos/518543/pexels-photo-518543.jpeg?auto=compress&cs=tinysrgb&w=960",
        "https://images.pexels.com/photos/210607/pexels-photo-210607.jpeg?auto=compress&cs=tinysrgb&w=960",
        "https://images.pexels.com/photos/373543/pexels-photo-373543.jpeg?auto=compress&cs=tinysrgb&w=960",
        "https://images.pexels.com/photos/3944454/pexels-photo-3944454.jpeg?auto=compress&cs=tinysrgb&w=960",
        "https://images.pexels.com/photos/1591060/pexels-photo-1591060.jpeg?auto=compress&cs=tinysrgb&w=960",
        "https://images.pexels.com/photos/87651/earth-blue-planet-globe-planet-87651.jpeg?auto=compress&cs=tinysrgb&w=960"
    )

    fun getFallbackPhotoForSection(section: NewsSectionType, index: Int): String {
        val list = SECTION_FALLBACK_PHOTOS[section] ?: SECTION_FALLBACK_PHOTOS.getValue(NewsSectionType.TOP_STORIES)
        return list[(index and 0x7FFFFFFF) % list.size]
    }

    fun getSecondaryFallbackPhoto(section: NewsSectionType, index: Int): String {
        val offset = section.ordinal + index
        return SECONDARY_FALLBACK_PHOTOS[(offset and 0x7FFFFFFF) % SECONDARY_FALLBACK_PHOTOS.size]
    }

    private val _newsState = MutableStateFlow(buildBaselineState("ar"))
    val newsState: StateFlow<NewsCenterState> = _newsState.asStateFlow()

    fun ensureInitialized(context: Context, appLanguage: String, forceRefresh: Boolean = false) {
        val effLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        val current = _newsState.value
        val now = System.currentTimeMillis()
        val languageChanged = current.languageCode != effLang
        if (!forceRefresh && !languageChanged && (now - current.fetchedAtMillis) < 4 * 60 * 1000L && current.fetchedAtMillis > 0L) {
            return
        }
        if (languageChanged) {
            _newsState.value = buildBaselineState(effLang)
        }
        scope.launch {
            refreshNewsForLanguage(effLang, force = forceRefresh || languageChanged)
        }
    }

    suspend fun refreshNewsForLanguage(appLanguage: String, force: Boolean = false) {
        val effLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        fetchMutex.withLock {
            val current = _newsState.value
            val now = System.currentTimeMillis()
            if (!force && current.languageCode == effLang && (now - current.fetchedAtMillis) < 2 * 60 * 1000L && current.fetchedAtMillis > 0L) {
                return
            }

            val baseline = buildBaselineState(effLang)
            _newsState.value = if (current.languageCode == effLang && current.articlesBySection.isNotEmpty()) {
                current.copy(isLoading = true)
            } else {
                baseline.copy(isLoading = true)
            }

            val sourceConfig = getBestSourceConfigForLanguage(effLang)
            val strings = getLocalizedStrings(effLang)
            val updatedSections = baseline.articlesBySection.toMutableMap()

            // Fetch all 5 sections concurrently so real news and images appear quickly
            val sectionResults = coroutineScope {
                NewsSectionType.entries.map { section ->
                    async(Dispatchers.IO) {
                        val feedUrls = sourceConfig.sectionFeeds[section].orEmpty()
                        val fetchedArticles = mutableListOf<NewsArticleItem>()
                        for (feedUrl in feedUrls) {
                            if (fetchedArticles.size >= 6) break
                            val parsed = fetchAndParseRssFeed(
                                feedUrl = feedUrl,
                                section = section,
                                defaultCategoryLabel = getSectionCategoryBadge(section, strings),
                                defaultSourceName = resolveFeedSourceName(feedUrl, sourceConfig.primarySourceName, effLang),
                                effLang = effLang
                            )
                            for (item in parsed) {
                                if (fetchedArticles.none { it.headline.equals(item.headline, ignoreCase = true) }) {
                                    fetchedArticles.add(item)
                                }
                                if (fetchedArticles.size >= 6) break
                            }
                        }
                        section to fetchedArticles
                    }
                }.awaitAll()
            }

            for ((section, fetchedArticles) in sectionResults) {
                if (fetchedArticles.isNotEmpty()) {
                    val fallbackList = baseline.articlesBySection[section].orEmpty()
                    val combined = (fetchedArticles + fallbackList)
                        .distinctBy { it.headline }
                        .take(6)
                    updatedSections[section] = combined
                }
            }

            // Build breaking news ticker from the top headlines across sections
            val liveTicker = updatedSections[NewsSectionType.TOP_STORIES]
                .orEmpty()
                .take(4)
                .map { it.headline }
                .ifEmpty { baseline.breakingTickerItems }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            _newsState.value = NewsCenterState(
                languageCode = effLang,
                activeSourceName = sourceConfig.primarySourceName,
                articlesBySection = updatedSections,
                breakingTickerItems = liveTicker,
                updatedTime = timeFormat.format(Date()),
                fetchedAtMillis = System.currentTimeMillis(),
                isLoading = false
            )
        }
    }

    private fun resolveFeedSourceName(feedUrl: String, fallback: String, effLang: String): String {
        val lower = feedUrl.lowercase(Locale.US)
        val isAr = effLang in setOf("ar", "fa", "ur")
        return when {
            lower.contains("france24.com") -> if (isAr) "فرانس 24" else "France 24"
            lower.contains("skynewsarabia.com") -> if (isAr) "سكاي نيوز عربية" else "Sky News Arabia"
            lower.contains("bbci.co.uk/arabic") -> "BBC عربي"
            lower.contains("arabic.rt.com") -> if (isAr) "RT عربي" else "RT Arabic"
            lower.contains("aljazeera.net") -> if (isAr) "الجزيرة" else "Al Jazeera"
            lower.contains("aljazeera.com") -> "Al Jazeera"
            lower.contains("bbci.co.uk") -> "BBC News"
            lower.contains("trthaber.com") -> "TRT Haber"
            lower.contains("tagesschau.de") -> "Tagesschau"
            else -> fallback.substringBefore("•").trim()
        }
    }

    /**
     * Automatically selects the best native, image-rich news source and RSS feeds for each language.
     */
    fun getBestSourceConfigForLanguage(effLang: String): LanguageNewsSourceConfig {
        return when (effLang) {
            "ar" -> LanguageNewsSourceConfig(
                langCode = "ar",
                primarySourceName = "فرانس 24 • سكاي نيوز • BBC عربي",
                secondarySourceName = "الجزيرة",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.france24.com/ar/rss",
                        "https://www.skynewsarabia.com/rss.xml",
                        "https://feeds.bbci.co.uk/arabic/rss.xml",
                        "https://arabic.rt.com/rss/"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.france24.com/ar/middle-east/rss",
                        "https://www.skynewsarabia.com/web/rss/middle-east.xml",
                        "https://feeds.bbci.co.uk/arabic/middleeast/rss.xml"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.skynewsarabia.com/web/rss/business.xml",
                        "https://www.france24.com/ar/top-stories/rss",
                        "https://feeds.bbci.co.uk/arabic/rss.xml"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.skynewsarabia.com/web/rss/technology.xml",
                        "https://arabic.rt.com/rss/",
                        "https://www.france24.com/ar/rss"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.skynewsarabia.com/web/rss/world.xml",
                        "https://www.france24.com/ar/rss",
                        "https://feeds.bbci.co.uk/arabic/rss.xml"
                    )
                )
            )
            "fr" -> LanguageNewsSourceConfig(
                langCode = "fr",
                primarySourceName = "France 24 • Le Monde",
                secondarySourceName = "France Info",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://www.france24.com/fr/rss"),
                    NewsSectionType.POLITICS to listOf("https://www.france24.com/fr/france/rss"),
                    NewsSectionType.ECONOMY to listOf("https://www.france24.com/fr/%C3%A9co-tech/rss"),
                    NewsSectionType.TECHNOLOGY to listOf("https://www.france24.com/fr/%C3%A9co-tech/rss"),
                    NewsSectionType.WORLD to listOf("https://www.france24.com/fr/monde/rss")
                )
            )
            "es" -> LanguageNewsSourceConfig(
                langCode = "es",
                primarySourceName = "BBC Mundo • France 24 ES",
                secondarySourceName = "El País",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://feeds.bbci.co.uk/mundo/rss.xml", "https://www.france24.com/es/rss"),
                    NewsSectionType.POLITICS to listOf("https://www.france24.com/es/am%C3%A9rica-latina/rss"),
                    NewsSectionType.ECONOMY to listOf("https://www.france24.com/es/econom%C3%ADa-y-tecnolog%C3%ADa/rss"),
                    NewsSectionType.TECHNOLOGY to listOf("https://www.france24.com/es/econom%C3%ADa-y-tecnolog%C3%ADa/rss"),
                    NewsSectionType.WORLD to listOf("https://www.france24.com/es/rss")
                )
            )
            "tr" -> LanguageNewsSourceConfig(
                langCode = "tr",
                primarySourceName = "TRT Haber • BBC Türkçe",
                secondarySourceName = "Anadolu Ajansı",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://www.trthaber.com/sondakika.rss", "https://feeds.bbci.co.uk/turkce/rss.xml"),
                    NewsSectionType.POLITICS to listOf("https://www.trthaber.com/gundem_articles.rss"),
                    NewsSectionType.ECONOMY to listOf("https://www.trthaber.com/ekonomi_articles.rss"),
                    NewsSectionType.TECHNOLOGY to listOf("https://www.trthaber.com/bilim_teknoloji_articles.rss"),
                    NewsSectionType.WORLD to listOf("https://www.trthaber.com/dunya_articles.rss")
                )
            )
            "de" -> LanguageNewsSourceConfig(
                langCode = "de",
                primarySourceName = "Tagesschau • DW",
                secondarySourceName = "Spiegel",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://www.tagesschau.de/infoservices/alle-meldungen-100~rss2.xml"),
                    NewsSectionType.POLITICS to listOf("https://www.tagesschau.de/inland/index~rss2.xml"),
                    NewsSectionType.ECONOMY to listOf("https://www.tagesschau.de/wirtschaft/index~rss2.xml"),
                    NewsSectionType.TECHNOLOGY to listOf("https://www.tagesschau.de/wissen/index~rss2.xml"),
                    NewsSectionType.WORLD to listOf("https://www.tagesschau.de/ausland/index~rss2.xml")
                )
            )
            "ru" -> LanguageNewsSourceConfig(
                langCode = "ru",
                primarySourceName = "BBC News Русский",
                secondarySourceName = "Euronews RU",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://feeds.bbci.co.uk/russian/rss.xml"),
                    NewsSectionType.POLITICS to listOf("https://feeds.bbci.co.uk/russian/rss.xml"),
                    NewsSectionType.ECONOMY to listOf("https://feeds.bbci.co.uk/russian/rss.xml"),
                    NewsSectionType.TECHNOLOGY to listOf("https://feeds.bbci.co.uk/russian/rss.xml"),
                    NewsSectionType.WORLD to listOf("https://feeds.bbci.co.uk/russian/rss.xml")
                )
            )
            "ur" -> LanguageNewsSourceConfig(
                langCode = "ur",
                primarySourceName = "BBC News اردو",
                secondarySourceName = "GEO News",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://feeds.bbci.co.uk/urdu/rss.xml"),
                    NewsSectionType.POLITICS to listOf("https://feeds.bbci.co.uk/urdu/rss.xml"),
                    NewsSectionType.ECONOMY to listOf("https://feeds.bbci.co.uk/urdu/rss.xml"),
                    NewsSectionType.TECHNOLOGY to listOf("https://feeds.bbci.co.uk/urdu/rss.xml"),
                    NewsSectionType.WORLD to listOf("https://feeds.bbci.co.uk/urdu/rss.xml")
                )
            )
            "fa" -> LanguageNewsSourceConfig(
                langCode = "fa",
                primarySourceName = "BBC News فارسی",
                secondarySourceName = "Euronews FA",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf("https://feeds.bbci.co.uk/persian/rss.xml"),
                    NewsSectionType.POLITICS to listOf("https://feeds.bbci.co.uk/persian/rss.xml"),
                    NewsSectionType.ECONOMY to listOf("https://feeds.bbci.co.uk/persian/rss.xml"),
                    NewsSectionType.TECHNOLOGY to listOf("https://feeds.bbci.co.uk/persian/rss.xml"),
                    NewsSectionType.WORLD to listOf("https://feeds.bbci.co.uk/persian/rss.xml")
                )
            )
            else -> LanguageNewsSourceConfig(
                langCode = "en",
                primarySourceName = "BBC News • Al Jazeera",
                secondarySourceName = "France 24",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://feeds.bbci.co.uk/news/rss.xml",
                        "https://www.france24.com/en/rss",
                        "https://www.aljazeera.com/xml/rss/all.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://feeds.bbci.co.uk/news/politics/rss.xml",
                        "https://www.france24.com/en/rss"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://feeds.bbci.co.uk/news/business/rss.xml",
                        "https://www.france24.com/en/business-tech/rss"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://feeds.bbci.co.uk/news/technology/rss.xml",
                        "https://www.france24.com/en/business-tech/rss"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://feeds.bbci.co.uk/news/world/rss.xml",
                        "https://www.france24.com/en/rss"
                    )
                )
            )
        }
    }

    private fun fetchAndParseRssFeed(
        feedUrl: String,
        section: NewsSectionType,
        defaultCategoryLabel: String,
        defaultSourceName: String,
        effLang: String
    ): List<NewsArticleItem> {
        val xmlContent = httpGet(feedUrl, timeoutMs = 4500) ?: return emptyList()
        val results = mutableListOf<NewsArticleItem>()
        val themes = listOf("trading_floor", "diplomacy_summit", "ai_datacenter", "global_climate", "central_bank", "cyber_chip")

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var insideItem = false
            var insideImageTag = false
            var title = ""
            var description = ""
            var source = ""
            var articleLink = ""
            var imageUrl = ""
            var ogFetchesUsed = 0
            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT && results.size < 6) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name.lowercase(Locale.US)
                        if (tagName == "item" || tagName == "entry") {
                            insideItem = true
                            insideImageTag = false
                            title = ""
                            description = ""
                            source = ""
                            articleLink = ""
                            imageUrl = ""
                        } else if (insideItem) {
                            when {
                                tagName == "title" -> {
                                    title = cleanHtmlText(parser.nextText())
                                }
                                tagName == "link" -> {
                                    val hrefAttr = parser.getAttributeValue(null, "href").orEmpty()
                                    val textLink = try { parser.nextText().trim() } catch (_: Exception) { "" }
                                    val candidate = hrefAttr.ifBlank { textLink }
                                    if (candidate.startsWith("http") && articleLink.isBlank()) {
                                        articleLink = candidate
                                    }
                                }
                                tagName == "description" || tagName == "summary" -> {
                                    val rawDesc = parser.nextText()
                                    if (imageUrl.isBlank()) {
                                        imageUrl = normalizeNewsImageUrl(extractImgFromHtml(rawDesc))
                                    }
                                    description = cleanHtmlText(rawDesc)
                                }
                                tagName == "content:encoded" || tagName == "content" -> {
                                    val rawContent = try { parser.nextText() } catch (_: Exception) { "" }
                                    if (imageUrl.isBlank()) {
                                        imageUrl = normalizeNewsImageUrl(extractImgFromHtml(rawContent))
                                    }
                                    if (description.length < 30) {
                                        val cleaned = cleanHtmlText(rawContent)
                                        if (cleaned.length > description.length) description = cleaned
                                    }
                                }
                                tagName == "source" -> {
                                    source = cleanHtmlText(parser.nextText())
                                }
                                tagName == "media:thumbnail" || tagName == "media:content" || tagName == "enclosure" || tagName == "itunes:image" -> {
                                    val urlAttr = parser.getAttributeValue(null, "url")
                                        ?: parser.getAttributeValue(null, "href")
                                        ?: ""
                                    val typeAttr = parser.getAttributeValue(null, "type").orEmpty().lowercase(Locale.US)
                                    val mediumAttr = parser.getAttributeValue(null, "medium").orEmpty().lowercase(Locale.US)
                                    val isVideoOrAudio = typeAttr.startsWith("video/") || typeAttr.startsWith("audio/") ||
                                        mediumAttr == "video" || mediumAttr == "audio" ||
                                        urlAttr.contains(".mp4", true) || urlAttr.contains(".m3u8", true) ||
                                        urlAttr.contains(".mp3", true) || urlAttr.contains(".mov", true)
                                    val isLikelyImage = !isVideoOrAudio && (
                                        typeAttr.isBlank() || typeAttr.startsWith("image/") ||
                                        mediumAttr == "image" ||
                                        urlAttr.contains(".jpg", true) || urlAttr.contains(".jpeg", true) ||
                                        urlAttr.contains(".png", true) || urlAttr.contains(".webp", true) ||
                                        tagName == "media:thumbnail" || tagName == "itunes:image"
                                    )
                                    val normalized = normalizeNewsImageUrl(urlAttr)
                                    if (isLikelyImage && normalized.isNotBlank()) {
                                        if (imageUrl.isBlank() || (tagName == "media:content" && !imageUrl.contains("france24"))) {
                                            imageUrl = normalized
                                        }
                                    }
                                }
                                tagName == "image" -> {
                                    val urlAttr = parser.getAttributeValue(null, "url").orEmpty()
                                    val normalized = normalizeNewsImageUrl(urlAttr)
                                    if (normalized.isNotBlank() && imageUrl.isBlank()) {
                                        imageUrl = normalized
                                    } else {
                                        insideImageTag = true
                                    }
                                }
                                tagName == "url" && insideImageTag -> {
                                    val textUrl = try { parser.nextText() } catch (_: Exception) { "" }
                                    val normalized = normalizeNewsImageUrl(textUrl)
                                    if (normalized.isNotBlank() && imageUrl.isBlank()) {
                                        imageUrl = normalized
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name.lowercase(Locale.US)
                        if (tagName == "image") {
                            insideImageTag = false
                        } else if ((tagName == "item" || tagName == "entry") && insideItem) {
                            insideItem = false
                            insideImageTag = false
                            var cleanTitle = title.trim()
                            var resolvedSource = source.ifBlank { defaultSourceName.substringBefore("•").trim() }
                            if (cleanTitle.contains(" - ")) {
                                val afterDash = cleanTitle.substringAfterLast(" - ").trim()
                                val beforeDash = cleanTitle.substringBeforeLast(" - ").trim()
                                if (afterDash.length in 2..28 && beforeDash.length > 15) {
                                    cleanTitle = beforeDash
                                    if (source.isBlank()) resolvedSource = afterDash
                                }
                            }

                            if (cleanTitle.length >= 10) {
                                val idx = results.size
                                // If RSS item lacked an embedded photo, quickly extract og:image from the article page
                                if (imageUrl.isBlank() && articleLink.startsWith("http") &&
                                    !articleLink.contains("news.google.com") && ogFetchesUsed < 3
                                ) {
                                    ogFetchesUsed++
                                    imageUrl = extractOgImageFromArticleUrl(articleLink)
                                }

                                // Guarantee every single article has a real, crisp editorial photo URL
                                val finalImageUrl = imageUrl.ifBlank {
                                    getFallbackPhotoForSection(section, idx)
                                }

                                val timeAgoText = getRelativeTimeLabel(effLang, (idx + 1) * 6)
                                val summaryText = if (description.length >= 25 && !description.equals(cleanTitle, ignoreCase = true)) {
                                    description.take(200)
                                } else {
                                    getFallbackSummaryForSection(section, effLang, cleanTitle)
                                }

                                results.add(
                                    NewsArticleItem(
                                        id = "${section.id}_live_$idx",
                                        section = section,
                                        categoryLabel = defaultCategoryLabel,
                                        headline = cleanTitle,
                                        summary = summaryText,
                                        sourceName = resolvedSource,
                                        timeAgo = timeAgoText,
                                        imageUrl = finalImageUrl,
                                        visualTheme = themes[idx % themes.size]
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}

        return results
    }

    private fun extractOgImageFromArticleUrl(articleUrl: String): String {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(articleUrl)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2200
                readTimeout = 2200
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                setRequestProperty("Accept", "text/html,application/xhtml+xml")
            }
            if (conn.responseCode in 200..299) {
                val sb = StringBuilder(24576)
                BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { reader ->
                    val buf = CharArray(4096)
                    var total = 0
                    while (total < 24576) {
                        val read = reader.read(buf)
                        if (read <= 0) break
                        sb.append(buf, 0, read)
                        total += read
                        if (sb.indexOf("</head>", ignoreCase = true) >= 0) break
                    }
                }
                val head = sb.toString()
                val patterns = listOf(
                    Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
                    Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""", RegexOption.IGNORE_CASE),
                    Regex("""<meta[^>]+name=["']twitter:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                )
                for (p in patterns) {
                    val found = p.find(head)?.groupValues?.getOrNull(1)
                    val normalized = normalizeNewsImageUrl(found.orEmpty())
                    if (normalized.isNotBlank()) return normalized
                }
            }
            ""
        } catch (_: Exception) {
            ""
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {}
        }
    }

    private fun normalizeNewsImageUrl(raw: String): String {
        var url = raw.trim()
            .replace("&amp;", "&")
            .replace("&#38;", "&")
            .replace(" ", "%20")
        if (url.isBlank()) return ""
        if (url.startsWith("//")) url = "https:$url"
        if (url.startsWith("http://")) url = url.replaceFirst("http://", "https://")
        if (!url.startsWith("https://")) return ""

        val lower = url.lowercase(Locale.US)
        if (lower.endsWith(".svg") || lower.endsWith(".mp4") || lower.endsWith(".m3u8") ||
            lower.endsWith(".mp3") || lower.endsWith(".avif") ||
            lower.contains("1x1") || lower.contains("pixel") || lower.contains("favicon")
        ) {
            return ""
        }

        // Only upgrade legacy BBC /news/240/ paths; preserve /ace/standard/ URLs intact so they never 404
        if (lower.contains("ichef.bbci.co.uk/news/240/")) {
            url = url.replace("/news/240/", "/news/624/")
        }

        return url
    }

    private fun extractImgFromHtml(raw: String): String {
        val patterns = listOf(
            Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
            Regex("""<img[^>]+data-src=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
            Regex("""srcset=["']([^"'\s]+)""", RegexOption.IGNORE_CASE)
        )
        for (regex in patterns) {
            val match = regex.find(raw)
            val url = match?.groupValues?.getOrNull(1).orEmpty()
            if (url.startsWith("http") || url.startsWith("//")) return url
        }
        return ""
    }

    private fun cleanHtmlText(raw: String): String {
        return raw
            .replace(Regex("<[^>]*>"), " ")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun getRelativeTimeLabel(effLang: String, minutes: Int): String {
        return when (effLang) {
            "ar", "fa", "ur" -> "منذ $minutes دقيقة"
            "fr" -> "il y a $minutes min"
            "es" -> "hace $minutes min"
            "tr" -> "$minutes dk önce"
            "de" -> "vor $minutes Min."
            "ru" -> "$minutes мин. назад"
            else -> "$minutes min ago"
        }
    }

    private fun getFallbackSummaryForSection(section: NewsSectionType, effLang: String, title: String): String {
        return if (effLang in setOf("ar", "fa", "ur")) {
            when (section) {
                NewsSectionType.TOP_STORIES -> "تغطية إخبارية شاملة ومباشرة لأبرز التطورات والأحداث الجارية وتأثيراتها الإقليمية والدولية."
                NewsSectionType.POLITICS -> "متابعة خاصة للمباحثات الدبلوماسية والقرارات السياسية الرسمية والمواقف الدولية الراهنة."
                NewsSectionType.ECONOMY -> "تحليل معمق لحركة الأسواق العالمية ومؤشرات التضخم وأسعار الفائدة والعملات."
                NewsSectionType.TECHNOLOGY -> "أحدث الابتكارات التقنية وتطورات الذكاء الاصطناعي وقطاع الشركات التكنولوجية الكبرى."
                NewsSectionType.WORLD -> "تقارير ميدانية من مختلف العواصم العالمية حول أبرز القضايا والقمم الدولية."
            }
        } else {
            "Live coverage and in-depth analysis on: $title."
        }
    }

    private fun getSectionCategoryBadge(section: NewsSectionType, strings: NewsLocalizedStrings): String {
        return when (section) {
            NewsSectionType.TOP_STORIES -> strings.sectionTopStories.uppercase(Locale.US)
            NewsSectionType.POLITICS -> strings.sectionPolitics.uppercase(Locale.US)
            NewsSectionType.ECONOMY -> strings.sectionEconomy.uppercase(Locale.US)
            NewsSectionType.TECHNOLOGY -> strings.sectionTechnology.uppercase(Locale.US)
            NewsSectionType.WORLD -> strings.sectionWorld.uppercase(Locale.US)
        }
    }

    private fun httpGet(urlString: String, timeoutMs: Int = 4500): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*")
            }
            if (conn.responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            } else null
        } catch (_: Exception) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {}
        }
    }

    private fun buildBaselineState(effLang: String): NewsCenterState {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val isAr = effLang in setOf("ar", "fa", "ur")
        val sourceConfig = getBestSourceConfigForLanguage(effLang)

        if (isAr) {
            val topStoriesAr = listOf(
                NewsArticleItem(
                    id = "ar_top_1",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "اقتصاد",
                    headline = "انتعاش الأسواق العالمية مع تباطؤ التضخم بوتيرة أسرع من التوقعات",
                    summary = "سجلت المؤشرات العالمية مستويات قياسية اليوم بعد أن كشفت أحدث بيانات مؤشر أسعار المستهلكين عن تراجع حاد في معدلات التضخم.",
                    sourceName = "فرانس 24",
                    timeAgo = "منذ 12 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 0),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "ar_top_2",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "تكنولوجيا",
                    headline = "عملاق التكنولوجيا يعلن عن نموذج ذكاء اصطناعي فائق القدرات",
                    summary = "الجيل الجديد من نماذج الذكاء الاصطناعي يوفر معالجة فورية للبيانات متعددة الوسائط بكفاءة طاقة غير مسبوقة.",
                    sourceName = "BBC عربي",
                    timeAgo = "منذ 18 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 1),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "ar_top_3",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "حول العالم",
                    headline = "التوصل إلى اتفاق مناخي جديد خلال القمة العالمية للطاقة المستدامة",
                    summary = "اتفق قادة الدول المشاركة على تسريع الاستثمارات في مشاريع الطاقة النظيفة وخفض الانبعاثات الكربونية خلال العقد القادم.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = "منذ 25 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 0),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_top_4",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "اقتصاد",
                    headline = "البنك المركزي يقرر تثبيت أسعار الفائدة لدعم استقرار النمو الاقتصادي",
                    summary = "أكد صناع السياسة النقدية استمرار مراقبة مؤشرات الأسعار وسوق العمل قبل اتخاذ أي خطوات جديدة بشأن أسعار الفائدة.",
                    sourceName = "الجزيرة",
                    timeAgo = "منذ 34 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 3),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "ar_top_5",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "سياسة",
                    headline = "مباحثات دبلوماسية مكثفة لتعزيز التعاون الإقليمي والأمن المشترك",
                    summary = "شهدت العاصمة سلسلة لقاءات رفيعة المستوى لبحث ملفات التهدئة وتوسيع الشراكات الاستراتيجية والتجارية.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = "منذ 42 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_top_6",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "تكنولوجيا",
                    headline = "إطلاق الجيل الجديد من معالجات الحوسبة السحابية ورقائق المستقبل",
                    summary = "قفزة تقنية جديدة في صناعة أشباه الموصلات تتيح سرعات مضاعفة لمراكز البيانات والأجهزة الذكية.",
                    sourceName = "BBC عربي",
                    timeAgo = "منذ 50 دقيقة",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 0),
                    visualTheme = "cyber_chip"
                )
            )

            val politicsAr = listOf(
                topStoriesAr[4],
                topStoriesAr[2].copy(id = "ar_pol_2", section = NewsSectionType.POLITICS, categoryLabel = "سياسة", imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 1)),
                topStoriesAr[0].copy(id = "ar_pol_3", section = NewsSectionType.POLITICS, categoryLabel = "سياسة", imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 2)),
                topStoriesAr[3].copy(id = "ar_pol_4", section = NewsSectionType.POLITICS, imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 3)),
                topStoriesAr[1].copy(id = "ar_pol_5", section = NewsSectionType.POLITICS, imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 4)),
                topStoriesAr[5].copy(id = "ar_pol_6", section = NewsSectionType.POLITICS, imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 5))
            )

            val economyAr = listOf(
                topStoriesAr[0].copy(id = "ar_eco_1", section = NewsSectionType.ECONOMY),
                topStoriesAr[3].copy(id = "ar_eco_2", section = NewsSectionType.ECONOMY),
                topStoriesAr[1].copy(id = "ar_eco_3", section = NewsSectionType.ECONOMY, imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 1)),
                topStoriesAr[2].copy(id = "ar_eco_4", section = NewsSectionType.ECONOMY, imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 2)),
                topStoriesAr[4].copy(id = "ar_eco_5", section = NewsSectionType.ECONOMY, imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 4)),
                topStoriesAr[5].copy(id = "ar_eco_6", section = NewsSectionType.ECONOMY, imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 5))
            )

            val techAr = listOf(
                topStoriesAr[1].copy(id = "ar_tech_1", section = NewsSectionType.TECHNOLOGY),
                topStoriesAr[5].copy(id = "ar_tech_2", section = NewsSectionType.TECHNOLOGY),
                topStoriesAr[0].copy(id = "ar_tech_3", section = NewsSectionType.TECHNOLOGY, imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 2)),
                topStoriesAr[2].copy(id = "ar_tech_4", section = NewsSectionType.TECHNOLOGY, imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 3)),
                topStoriesAr[3].copy(id = "ar_tech_5", section = NewsSectionType.TECHNOLOGY, imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 4)),
                topStoriesAr[4].copy(id = "ar_tech_6", section = NewsSectionType.TECHNOLOGY, imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 5))
            )

            val worldAr = listOf(
                topStoriesAr[2].copy(id = "ar_wld_1", section = NewsSectionType.WORLD),
                topStoriesAr[4].copy(id = "ar_wld_2", section = NewsSectionType.WORLD, imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 1)),
                topStoriesAr[0].copy(id = "ar_wld_3", section = NewsSectionType.WORLD, imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 2)),
                topStoriesAr[1].copy(id = "ar_wld_4", section = NewsSectionType.WORLD, imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 3)),
                topStoriesAr[3].copy(id = "ar_wld_5", section = NewsSectionType.WORLD, imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 4)),
                topStoriesAr[5].copy(id = "ar_wld_6", section = NewsSectionType.WORLD, imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 5))
            )

            return NewsCenterState(
                languageCode = effLang,
                activeSourceName = sourceConfig.primarySourceName,
                articlesBySection = mapOf(
                    NewsSectionType.TOP_STORIES to topStoriesAr,
                    NewsSectionType.POLITICS to politicsAr,
                    NewsSectionType.ECONOMY to economyAr,
                    NewsSectionType.TECHNOLOGY to techAr,
                    NewsSectionType.WORLD to worldAr
                ),
                breakingTickerItems = listOf(
                    "البنك المركزي يقرر تثبيت أسعار الفائدة دون تغيير",
                    "التوصل إلى اتفاق مناخي جديد خلال القمة العالمية",
                    "عملاق التكنولوجيا يكشف عن الجيل الجديد من نماذج الذكاء الاصطناعي",
                    "انتعاش الأسواق العالمية مع تباطؤ التضخم بوتيرة أسرع من المتوقع"
                ),
                updatedTime = timeFormat.format(Date()),
                fetchedAtMillis = 0L,
                isLoading = false
            )
        } else {
            val topStoriesEn = listOf(
                NewsArticleItem(
                    id = "en_top_1",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "ECONOMY",
                    headline = "MARKETS RALLY AS INFLATION COOLS FASTER THAN EXPECTED",
                    summary = "Global indexes reached record highs today as the latest consumer price index data revealed a sharper-than-forecast decline in inflation.",
                    sourceName = "BBC News",
                    timeAgo = "12 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 0),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "en_top_2",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "TECHNOLOGY",
                    headline = "Tech Giant Announces Breakthrough AI Model",
                    summary = "Next-generation multimodal reasoning architecture delivers real-time inference with unprecedented energy efficiency across cloud and edge devices.",
                    sourceName = "BBC News",
                    timeAgo = "18 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 1),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "en_top_3",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "WORLD",
                    headline = "NEW CLIMATE AGREEMENT REACHED AT GLOBAL SUMMIT",
                    summary = "World leaders agreed on a landmark clean-energy investment framework aimed at accelerating renewable infrastructure over the next decade.",
                    sourceName = "BBC News",
                    timeAgo = "24 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 0),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "en_top_4",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "ECONOMY",
                    headline = "CENTRAL BANK HOLDS INTEREST RATES STEADY",
                    summary = "Policymakers signaled a patient approach as inflation continues to moderate toward the long-term target alongside resilient labor data.",
                    sourceName = "Al Jazeera",
                    timeAgo = "32 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 3),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "en_top_5",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "POLITICS",
                    headline = "DIPLOMATIC TALKS ADVANCE ON REGIONAL TRADE CORRIDOR",
                    summary = "Ministers finalized key cross-border trade and digital infrastructure protocols following three days of high-level summit negotiations.",
                    sourceName = "BBC News",
                    timeAgo = "41 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "en_top_6",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = "TECHNOLOGY",
                    headline = "NEXT-GENERATION QUANTUM CHIP ACHIEVES MILESTONE",
                    summary = "Researchers demonstrated fault-tolerant logical qubits capable of executing complex simulations orders of magnitude faster.",
                    sourceName = "France 24",
                    timeAgo = "49 min ago",
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 0),
                    visualTheme = "cyber_chip"
                )
            )

            return NewsCenterState(
                languageCode = effLang,
                activeSourceName = sourceConfig.primarySourceName,
                articlesBySection = mapOf(
                    NewsSectionType.TOP_STORIES to topStoriesEn,
                    NewsSectionType.POLITICS to listOf(topStoriesEn[4], topStoriesEn[2], topStoriesEn[0], topStoriesEn[3], topStoriesEn[1], topStoriesEn[5]),
                    NewsSectionType.ECONOMY to listOf(topStoriesEn[0], topStoriesEn[3], topStoriesEn[1], topStoriesEn[2], topStoriesEn[4], topStoriesEn[5]),
                    NewsSectionType.TECHNOLOGY to listOf(topStoriesEn[1], topStoriesEn[5], topStoriesEn[0], topStoriesEn[2], topStoriesEn[3], topStoriesEn[4]),
                    NewsSectionType.WORLD to listOf(topStoriesEn[2], topStoriesEn[4], topStoriesEn[0], topStoriesEn[1], topStoriesEn[3], topStoriesEn[5])
                ),
                breakingTickerItems = listOf(
                    "Central bank holds interest rates steady",
                    "New climate agreement reached at global summit",
                    "Tech giant unveils next-generation AI model",
                    "Markets rally as inflation cools faster than expected"
                ),
                updatedTime = timeFormat.format(Date()),
                fetchedAtMillis = 0L,
                isLoading = false
            )
        }
    }

    fun getLocalizedStrings(langCode: String): NewsLocalizedStrings {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return when (eff) {
            "ar", "fa", "ur" -> NewsLocalizedStrings(
                newsCenterTitle = "مركز الأخبار",
                liveBadge = "مباشر",
                sectionsHeader = "الأقسام",
                sectionTopStories = "أهم الأخبار",
                sectionPolitics = "سياسة",
                sectionEconomy = "اقتصاد",
                sectionTechnology = "تكنولوجيا",
                sectionWorld = "حول العالم",
                upNextHeader = "الخبر التالي",
                upNextPrefix = "التالي: ",
                updatedPrefix = "تم التحديث ",
                breakingBadge = "عاجل",
                sourceLabel = "المصدر التلقائي: ",
                refreshAction = "تحديث الأخبار",
                backToHomeHint = "رجوع للشاشة الرئيسية"
            )
            "fr" -> NewsLocalizedStrings(
                newsCenterTitle = "CENTRE D'ACTUALITÉS",
                liveBadge = "EN DIRECT",
                sectionsHeader = "RUBRIQUES",
                sectionTopStories = "À la Une",
                sectionPolitics = "Politique",
                sectionEconomy = "Économie",
                sectionTechnology = "Technologie",
                sectionWorld = "Monde",
                upNextHeader = "À SUIVRE",
                upNextPrefix = "À suivre : ",
                updatedPrefix = "Mis à jour ",
                breakingBadge = "URGENT",
                sourceLabel = "Source : ",
                refreshAction = "Actualiser",
                backToHomeHint = "Retour à l'accueil"
            )
            "es" -> NewsLocalizedStrings(
                newsCenterTitle = "CENTRO DE NOTICIAS",
                liveBadge = "EN VIVO",
                sectionsHeader = "SECCIONES",
                sectionTopStories = "Destacadas",
                sectionPolitics = "Política",
                sectionEconomy = "Economía",
                sectionTechnology = "Tecnología",
                sectionWorld = "Mundo",
                upNextHeader = "A CONTINUACIÓN",
                upNextPrefix = "Siguiente: ",
                updatedPrefix = "Actualizado ",
                breakingBadge = "ÚLTIMA HORA",
                sourceLabel = "Fuente: ",
                refreshAction = "Actualizar",
                backToHomeHint = "Volver al inicio"
            )
            "tr" -> NewsLocalizedStrings(
                newsCenterTitle = "HABER MERKEZİ",
                liveBadge = "CANLI",
                sectionsHeader = "KATEGORİLER",
                sectionTopStories = "Manşetler",
                sectionPolitics = "Siyaset",
                sectionEconomy = "Ekonomi",
                sectionTechnology = "Teknoloji",
                sectionWorld = "Dünya",
                upNextHeader = "SIRADAKİ HABER",
                upNextPrefix = "Sıradaki: ",
                updatedPrefix = "Güncellendi ",
                breakingBadge = "SON DAKİKA",
                sourceLabel = "Kaynak: ",
                refreshAction = "Yenile",
                backToHomeHint = "Ana ekrana dön"
            )
            "de" -> NewsLocalizedStrings(
                newsCenterTitle = "NACHRICHTENZENTRUM",
                liveBadge = "LIVE",
                sectionsHeader = "RESSORTS",
                sectionTopStories = "Top-Meldungen",
                sectionPolitics = "Politik",
                sectionEconomy = "Wirtschaft",
                sectionTechnology = "Technologie",
                sectionWorld = "Welt",
                upNextHeader = "ALS NÄCHSTES",
                upNextPrefix = "Als Nächstes: ",
                updatedPrefix = "Aktualisiert ",
                breakingBadge = "EILMELDUNG",
                sourceLabel = "Quelle: ",
                refreshAction = "Aktualisieren",
                backToHomeHint = "Zurück zum Start"
            )
            "it" -> NewsLocalizedStrings(
                newsCenterTitle = "CENTRO NOTIZIE",
                liveBadge = "LIVE",
                sectionsHeader = "SEZIONI",
                sectionTopStories = "In evidenza",
                sectionPolitics = "Politica",
                sectionEconomy = "Economia",
                sectionTechnology = "Tecnologia",
                sectionWorld = "Mondo",
                upNextHeader = "PROSSIMA",
                upNextPrefix = "Prossima: ",
                updatedPrefix = "Aggiornato ",
                breakingBadge = "ULTIM'ORA",
                sourceLabel = "Fonte: ",
                refreshAction = "Aggiorna",
                backToHomeHint = "Torna alla Home"
            )
            "ru" -> NewsLocalizedStrings(
                newsCenterTitle = "ЦЕНТР НОВОСТЕЙ",
                liveBadge = "LIVE",
                sectionsHeader = "РАЗДЕЛЫ",
                sectionTopStories = "Главное",
                sectionPolitics = "Политика",
                sectionEconomy = "Экономика",
                sectionTechnology = "Технологии",
                sectionWorld = "Мир",
                upNextHeader = "ДАЛЕЕ",
                upNextPrefix = "Далее: ",
                updatedPrefix = "Обновлено ",
                breakingBadge = "СРОЧНО",
                sourceLabel = "Источник: ",
                refreshAction = "Обновить",
                backToHomeHint = "На главную"
            )
            "pt" -> NewsLocalizedStrings(
                newsCenterTitle = "CENTRAL DE NOTÍCIAS",
                liveBadge = "AO VIVO",
                sectionsHeader = "SEÇÕES",
                sectionTopStories = "Destaques",
                sectionPolitics = "Política",
                sectionEconomy = "Economia",
                sectionTechnology = "Tecnologia",
                sectionWorld = "Mundo",
                upNextHeader = "A SEGUIR",
                upNextPrefix = "A seguir: ",
                updatedPrefix = "Atualizado ",
                breakingBadge = "URGENTE",
                sourceLabel = "Fonte: ",
                refreshAction = "Atualizar",
                backToHomeHint = "Voltar ao Início"
            )
            "pl" -> NewsLocalizedStrings(
                newsCenterTitle = "CENTRUM WIADOMOŚCI",
                liveBadge = "NA ŻYWO",
                sectionsHeader = "DZIAŁY",
                sectionTopStories = "Najważniejsze",
                sectionPolitics = "Polityka",
                sectionEconomy = "Ekonomia",
                sectionTechnology = "Technologia",
                sectionWorld = "Świat",
                upNextHeader = "NASTĘPNE",
                upNextPrefix = "Następne: ",
                updatedPrefix = "Zaktualizowano ",
                breakingBadge = "PILNE",
                sourceLabel = "Źródło: ",
                refreshAction = "Odśwież",
                backToHomeHint = "Powrót"
            )
            "nl" -> NewsLocalizedStrings(
                newsCenterTitle = "NIEUWSCENTRUM",
                liveBadge = "LIVE",
                sectionsHeader = "RUBRIEKEN",
                sectionTopStories = "Hoofdpunten",
                sectionPolitics = "Politiek",
                sectionEconomy = "Economie",
                sectionTechnology = "Technologie",
                sectionWorld = "Wereld",
                upNextHeader = "VOLGENDE",
                upNextPrefix = "Volgende: ",
                updatedPrefix = "Bijgewerkt ",
                breakingBadge = "BREAKING",
                sourceLabel = "Bron: ",
                refreshAction = "Vernieuwen",
                backToHomeHint = "Terug naar start"
            )
            "el" -> NewsLocalizedStrings(
                newsCenterTitle = "ΚΕΝΤΡΟ ΕΙΔΗΣΕΩΝ",
                liveBadge = "ΖΩΝΤΑΝΑ",
                sectionsHeader = "ΕΝΟΤΗΤΕΣ",
                sectionTopStories = "Κυριότερα",
                sectionPolitics = "Πολιτική",
                sectionEconomy = "Οικονομία",
                sectionTechnology = "Τεχνολογία",
                sectionWorld = "Κόσμος",
                upNextHeader = "ΕΠΟΜΕΝΟ",
                upNextPrefix = "Επόμενο: ",
                updatedPrefix = "Ενημερώθηκε ",
                breakingBadge = "ΕΚΤΑΚΤΟ",
                sourceLabel = "Πηγή: ",
                refreshAction = "Ανανέωση",
                backToHomeHint = "Επιστροφή"
            )
            "id", "in" -> NewsLocalizedStrings(
                newsCenterTitle = "PUSAT BERITA",
                liveBadge = "LANGSUNG",
                sectionsHeader = "KATEGORI",
                sectionTopStories = "Berita Utama",
                sectionPolitics = "Politik",
                sectionEconomy = "Ekonomi",
                sectionTechnology = "Teknologi",
                sectionWorld = "Dunia",
                upNextHeader = "BERIKUTNYA",
                upNextPrefix = "Berikutnya: ",
                updatedPrefix = "Diperbarui ",
                breakingBadge = "TERKINI",
                sourceLabel = "Sumber: ",
                refreshAction = "Muat ulang",
                backToHomeHint = "Kembali ke Beranda"
            )
            "vi" -> NewsLocalizedStrings(
                newsCenterTitle = "TRUNG TÂM TIN TỨC",
                liveBadge = "TRỰC TIẾP",
                sectionsHeader = "CHUYÊN MỤC",
                sectionTopStories = "Tin nổi bật",
                sectionPolitics = "Chính trị",
                sectionEconomy = "Kinh tế",
                sectionTechnology = "Công nghệ",
                sectionWorld = "Thế giới",
                upNextHeader = "TIẾP THEO",
                upNextPrefix = "Tiếp theo: ",
                updatedPrefix = "Cập nhật ",
                breakingBadge = "TIN NÓNG",
                sourceLabel = "Nguồn: ",
                refreshAction = "Làm mới",
                backToHomeHint = "Về trang chủ"
            )
            "th" -> NewsLocalizedStrings(
                newsCenterTitle = "ศูนย์ข่าวสาร",
                liveBadge = "สด",
                sectionsHeader = "หมวดหมู่",
                sectionTopStories = "ข่าวเด่น",
                sectionPolitics = "การเมือง",
                sectionEconomy = "เศรษฐกิจ",
                sectionTechnology = "เทคโนโลยี",
                sectionWorld = "ต่างประเทศ",
                upNextHeader = "ข่าวถัดไป",
                upNextPrefix = "ถัดไป: ",
                updatedPrefix = "อัปเดต ",
                breakingBadge = "ข่าวด่วน",
                sourceLabel = "ที่มา: ",
                refreshAction = "รีเฟรช",
                backToHomeHint = "กลับหน้าหลัก"
            )
            "zh" -> NewsLocalizedStrings(
                newsCenterTitle = "新闻中心",
                liveBadge = "实时",
                sectionsHeader = "栏目",
                sectionTopStories = "头条新闻",
                sectionPolitics = "政治",
                sectionEconomy = "财经",
                sectionTechnology = "科技",
                sectionWorld = "国际",
                upNextHeader = "下一条",
                upNextPrefix = "下一条: ",
                updatedPrefix = "更新于 ",
                breakingBadge = "快讯",
                sourceLabel = "来源: ",
                refreshAction = "刷新",
                backToHomeHint = "返回首页"
            )
            "ja" -> NewsLocalizedStrings(
                newsCenterTitle = "ニュースセンター",
                liveBadge = "ライブ",
                sectionsHeader = "カテゴリ",
                sectionTopStories = "主要ニュース",
                sectionPolitics = "政治",
                sectionEconomy = "経済",
                sectionTechnology = "テクノロジー",
                sectionWorld = "国際",
                upNextHeader = "次の記事",
                upNextPrefix = "次へ: ",
                updatedPrefix = "更新 ",
                breakingBadge = "速報",
                sourceLabel = "出典: ",
                refreshAction = "更新",
                backToHomeHint = "ホームへ戻る"
            )
            "ko" -> NewsLocalizedStrings(
                newsCenterTitle = "뉴스 센터",
                liveBadge = "실시간",
                sectionsHeader = "섹션",
                sectionTopStories = "주요 뉴스",
                sectionPolitics = "정치",
                sectionEconomy = "경제",
                sectionTechnology = "기술",
                sectionWorld = "세계",
                upNextHeader = "다음 뉴스",
                upNextPrefix = "다음: ",
                updatedPrefix = "업데이트 ",
                breakingBadge = "속보",
                sourceLabel = "출처: ",
                refreshAction = "새로고침",
                backToHomeHint = "홈으로"
            )
            "hi" -> NewsLocalizedStrings(
                newsCenterTitle = "समाचार केंद्र",
                liveBadge = "लाइव",
                sectionsHeader = "श्रेणियाँ",
                sectionTopStories = "मुख्य समाचार",
                sectionPolitics = "राजनीति",
                sectionEconomy = "अर्थव्यवस्था",
                sectionTechnology = "प्रौद्योगिकी",
                sectionWorld = "विश्व",
                upNextHeader = "अगला समाचार",
                upNextPrefix = "अगला: ",
                updatedPrefix = "अपडेट ",
                breakingBadge = "ब्रेकिंग",
                sourceLabel = "स्रोत: ",
                refreshAction = "रिफ्रेश",
                backToHomeHint = "होम पर लौटें"
            )
            else -> NewsLocalizedStrings(
                newsCenterTitle = "NEWS CENTER",
                liveBadge = "LIVE",
                sectionsHeader = "SECTIONS",
                sectionTopStories = "Top Stories",
                sectionPolitics = "Politics",
                sectionEconomy = "Economy",
                sectionTechnology = "Technology",
                sectionWorld = "World",
                upNextHeader = "UP NEXT",
                upNextPrefix = "Up Next: ",
                updatedPrefix = "Updated ",
                breakingBadge = "BREAKING",
                sourceLabel = "Source: ",
                refreshAction = "Refresh",
                backToHomeHint = "Back to Home"
            )
        }
    }
}
