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

            // Fetch all 5 sections concurrently so real news and images appear quickly
            val sectionResults = coroutineScope {
                NewsSectionType.entries.map { section ->
                    async(Dispatchers.IO) {
                        val feedUrls = sourceConfig.sectionFeeds[section].orEmpty()
                        val fetchedArticles = mutableListOf<NewsArticleItem>()
                        for (feedUrl in feedUrls) {
                            if (fetchedArticles.size >= 16) break
                            val parsed = fetchAndParseRssFeed(
                                feedUrl = feedUrl,
                                section = section,
                                defaultCategoryLabel = getSectionCategoryBadge(section, strings),
                                defaultSourceName = resolveFeedSourceName(feedUrl, sourceConfig.primarySourceName, effLang),
                                effLang = effLang,
                                maxItems = 16
                            )
                            for (item in parsed) {
                                val norm = normalizeHeadlineKey(item.headline)
                                if (fetchedArticles.none { normalizeHeadlineKey(it.headline) == norm }) {
                                    fetchedArticles.add(item)
                                }
                                if (fetchedArticles.size >= 16) break
                            }
                        }
                        section to fetchedArticles
                    }
                }.awaitAll()
            }.toMap()

            val updatedSections = assignDistinctSectionArticles(
                candidatesBySection = sectionResults,
                baselineBySection = baseline.articlesBySection,
                strings = strings,
                effLang = effLang
            )

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

    internal fun normalizeHeadlineKey(headline: String): String {
        return headline
            .lowercase(Locale.US)
            .replace(Regex("[\\p{Punct}«»“”‘’—–…]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    internal fun assignDistinctSectionArticles(
        candidatesBySection: Map<NewsSectionType, List<NewsArticleItem>>,
        baselineBySection: Map<NewsSectionType, List<NewsArticleItem>>,
        strings: NewsLocalizedStrings,
        effLang: String
    ): Map<NewsSectionType, List<NewsArticleItem>> {
        val usedHeadlines = HashSet<String>()
        val result = linkedMapOf<NewsSectionType, List<NewsArticleItem>>()

        for (section in NewsSectionType.entries) {
            val candidates = candidatesBySection[section].orEmpty()
            val picked = mutableListOf<NewsArticleItem>()

            for (item in candidates) {
                val key = normalizeHeadlineKey(item.headline)
                if (key.isNotEmpty() && usedHeadlines.add(key)) {
                    picked.add(item)
                }
                if (picked.size >= 6) break
            }

            if (picked.isEmpty() && candidates.isNotEmpty()) {
                for (item in candidates) {
                    val key = normalizeHeadlineKey(item.headline)
                    if (picked.none { normalizeHeadlineKey(it.headline) == key }) {
                        picked.add(item)
                        if (key.isNotEmpty()) usedHeadlines.add(key)
                    }
                    if (picked.size >= 6) break
                }
            }

            if (picked.isEmpty()) {
                val fallback = baselineBySection[section].orEmpty()
                for (item in fallback) {
                    val key = normalizeHeadlineKey(item.headline)
                    if (key.isEmpty() || usedHeadlines.add(key)) {
                        picked.add(item)
                    }
                    if (picked.size >= 6) break
                }
                if (picked.isEmpty()) {
                    picked.addAll(fallback.take(6))
                }
            }

            val badge = getSectionCategoryBadge(section, strings)
            result[section] = picked.mapIndexed { idx, article ->
                if (article.id.startsWith("${section.id}_live_")) {
                    article.copy(
                        id = "${section.id}_live_$idx",
                        section = section,
                        categoryLabel = badge,
                        timeAgo = getRelativeTimeLabel(effLang, (idx + 1) * 6)
                    )
                } else {
                    article.copy(section = section)
                }
            }
        }
        return result
    }

    private fun resolveFeedSourceName(feedUrl: String, fallback: String, effLang: String): String {
        val lower = feedUrl.lowercase(Locale.US)
        val isRtl = effLang in setOf("ar", "fa", "ur")
        return when {
            lower.contains("france24.com") -> if (isRtl) "فرانس 24" else "France 24"
            lower.contains("skynewsarabia.com") -> if (isRtl) "سكاي نيوز عربية" else "Sky News Arabia"
            lower.contains("bbci.co.uk/arabic") -> "BBC عربي"
            lower.contains("bbci.co.uk/persian") -> "BBC فارسی"
            lower.contains("bbci.co.uk/urdu") -> "BBC اردو"
            lower.contains("bbci.co.uk/russian") -> "BBC Русский"
            lower.contains("bbci.co.uk/mundo") -> "BBC Mundo"
            lower.contains("bbci.co.uk/turkce") -> "BBC Türkçe"
            lower.contains("arabic.rt.com") -> if (isRtl) "RT عربي" else "RT Arabic"
            lower.contains("aljazeera.net") -> if (isRtl) "الجزيرة" else "Al Jazeera"
            lower.contains("aljazeera.com") -> "Al Jazeera"
            lower.contains("bbci.co.uk") -> "BBC News"
            lower.contains("trthaber.com") -> "TRT Haber"
            lower.contains("tagesschau.de") -> "Tagesschau"
            lower.contains("lemonde.fr") -> "Le Monde"
            lower.contains("elpais.com") -> "El País"
            lower.contains("ansa.it") -> "ANSA"
            lower.contains("g1.globo.com") -> "G1 Globo"
            lower.contains("lenta.ru") -> "Lenta.ru"
            lower.contains("nos.nl") -> "NOS Nieuws"
            lower.contains("vnexpress.net") -> "VnExpress"
            lower.contains("antaranews.com") -> "Antara News"
            lower.contains("nhk.or.jp") -> "NHK"
            lower.contains("yna.co.kr") -> "연합뉴스"
            lower.contains("aajtak.in") -> "Aaj Tak"
            lower.contains("wiadomosci.wp.pl") -> "Wirtualna Polska"
            lower.contains("in.gr") -> "in.gr"
            lower.contains("rfi.fr") -> if (effLang == "fa") "RFI فارسی" else "RFI"
            lower.contains("isna.ir") -> "ایسنا"
            lower.contains("independentpersian.com") -> "ایندیپندنت فارسی"
            lower.contains("express.pk") -> "ایکسپریس نیوز"
            lower.contains("news.google.com") -> "Google News"
            else -> fallback.substringBefore("•").trim()
        }
    }

    /**
     * Automatically selects the best native, image-rich news source and RSS feeds for each language.
     */
    fun getBestSourceConfigForLanguage(effLang: String): LanguageNewsSourceConfig {
        return when (LocaleHelper.getEffectiveLanguage(effLang)) {
            "ar" -> LanguageNewsSourceConfig(
                langCode = "ar",
                primarySourceName = "فرانس 24 • سكاي نيوز • BBC عربي",
                secondarySourceName = "الجزيرة",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.france24.com/ar/rss",
                        "https://www.skynewsarabia.com/rss.xml",
                        "https://feeds.bbci.co.uk/arabic/rss.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.france24.com/ar/middle-east/rss",
                        "https://www.skynewsarabia.com/web/rss/middle-east.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=ar&gl=AE&ceid=AE:ar"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.france24.com/ar/%D8%A7%D9%82%D8%AA%D8%B5%D8%A7%D8%AF/rss",
                        "https://www.skynewsarabia.com/web/rss/business.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ar&gl=AE&ceid=AE:ar"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.skynewsarabia.com/web/rss/technology.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=ar&gl=AE&ceid=AE:ar"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.skynewsarabia.com/web/rss/world.xml",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=ar&gl=AE&ceid=AE:ar",
                        "https://www.france24.com/ar/rss"
                    )
                )
            )
            "fr" -> LanguageNewsSourceConfig(
                langCode = "fr",
                primarySourceName = "France 24 • Le Monde",
                secondarySourceName = "Google Actualités",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.france24.com/fr/rss",
                        "https://www.lemonde.fr/rss/une.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.lemonde.fr/politique/rss_full.xml",
                        "https://www.france24.com/fr/france/rss",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=fr&gl=FR&ceid=FR:fr"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.lemonde.fr/economie/rss_full.xml",
                        "https://www.france24.com/fr/%C3%A9co-tech/rss",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=fr&gl=FR&ceid=FR:fr"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.lemonde.fr/pixels/rss_full.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=fr&gl=FR&ceid=FR:fr"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.lemonde.fr/international/rss_full.xml",
                        "https://www.france24.com/fr/monde/rss",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=fr&gl=FR&ceid=FR:fr"
                    )
                )
            )
            "es" -> LanguageNewsSourceConfig(
                langCode = "es",
                primarySourceName = "El País • France 24 ES • BBC Mundo",
                secondarySourceName = "Google Noticias",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://feeds.bbci.co.uk/mundo/rss.xml",
                        "https://www.france24.com/es/rss"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.france24.com/es/am%C3%A9rica-latina/rss",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=es&gl=ES&ceid=ES:es"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/section/economia/portada",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=es&gl=ES&ceid=ES:es"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/section/tecnologia/portada",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=es&gl=ES&ceid=ES:es"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/section/internacional/portada",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=es&gl=ES&ceid=ES:es"
                    )
                )
            )
            "tr" -> LanguageNewsSourceConfig(
                langCode = "tr",
                primarySourceName = "TRT Haber • BBC Türkçe",
                secondarySourceName = "Google Haberler",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.trthaber.com/sondakika.rss",
                        "https://feeds.bbci.co.uk/turkce/rss.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.trthaber.com/gundem_articles.rss",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=tr&gl=TR&ceid=TR:tr"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.trthaber.com/ekonomi_articles.rss",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=tr&gl=TR&ceid=TR:tr"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.trthaber.com/bilim_teknoloji_articles.rss",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=tr&gl=TR&ceid=TR:tr"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.trthaber.com/dunya_articles.rss",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=tr&gl=TR&ceid=TR:tr"
                    )
                )
            )
            "de" -> LanguageNewsSourceConfig(
                langCode = "de",
                primarySourceName = "Tagesschau • DW",
                secondarySourceName = "Google News DE",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.tagesschau.de/infoservices/alle-meldungen-100~rss2.xml",
                        "https://news.google.com/rss?hl=de&gl=DE&ceid=DE:de"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.tagesschau.de/inland/index~rss2.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=de&gl=DE&ceid=DE:de"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.tagesschau.de/wirtschaft/index~rss2.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=de&gl=DE&ceid=DE:de"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.tagesschau.de/wissen/index~rss2.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=de&gl=DE&ceid=DE:de"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.tagesschau.de/ausland/index~rss2.xml",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=de&gl=DE&ceid=DE:de"
                    )
                )
            )
            "it" -> LanguageNewsSourceConfig(
                langCode = "it",
                primarySourceName = "ANSA • Google News IT",
                secondarySourceName = "ANSA",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.ansa.it/sito/ansait_rss.xml",
                        "https://news.google.com/rss?hl=it&gl=IT&ceid=IT:it"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.ansa.it/sito/notizie/politica/politica_rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=it&gl=IT&ceid=IT:it"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.ansa.it/sito/notizie/economia/economia_rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=it&gl=IT&ceid=IT:it"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.ansa.it/sito/notizie/tecnologia/tecnologia_rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=it&gl=IT&ceid=IT:it"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.ansa.it/sito/notizie/mondo/mondo_rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=it&gl=IT&ceid=IT:it"
                    )
                )
            )
            "pt" -> LanguageNewsSourceConfig(
                langCode = "pt",
                primarySourceName = "G1 Globo • Google Notícias",
                secondarySourceName = "G1",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://g1.globo.com/rss/g1/",
                        "https://news.google.com/rss?hl=pt-BR&gl=BR&ceid=BR:pt-419"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://g1.globo.com/rss/g1/politica/",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=pt-BR&gl=BR&ceid=BR:pt-419"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://g1.globo.com/rss/g1/economia/",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=pt-BR&gl=BR&ceid=BR:pt-419"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://g1.globo.com/rss/g1/tecnologia/",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=pt-BR&gl=BR&ceid=BR:pt-419"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://g1.globo.com/rss/g1/mundo/",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=pt-BR&gl=BR&ceid=BR:pt-419"
                    )
                )
            )
            "ru" -> LanguageNewsSourceConfig(
                langCode = "ru",
                primarySourceName = "Lenta.ru • BBC Русский",
                secondarySourceName = "Google Новости",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://feeds.bbci.co.uk/russian/rss.xml",
                        "https://lenta.ru/rss/news"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://lenta.ru/rss/news/russia",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=ru&gl=RU&ceid=RU:ru"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://lenta.ru/rss/news/economics",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ru&gl=RU&ceid=RU:ru"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://lenta.ru/rss/news/science",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=ru&gl=RU&ceid=RU:ru"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://lenta.ru/rss/news/world",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=ru&gl=RU&ceid=RU:ru"
                    )
                )
            )
            "nl" -> LanguageNewsSourceConfig(
                langCode = "nl",
                primarySourceName = "NOS Nieuws • Google Nieuws",
                secondarySourceName = "NOS",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://feeds.nos.nl/nosnieuwsalgemeen",
                        "https://news.google.com/rss?hl=nl&gl=NL&ceid=NL:nl"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://feeds.nos.nl/nosnieuwspolitiek",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=nl&gl=NL&ceid=NL:nl"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://feeds.nos.nl/nosnieuwseconomie",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=nl&gl=NL&ceid=NL:nl"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://feeds.nos.nl/nosnieuwstech",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=nl&gl=NL&ceid=NL:nl"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://feeds.nos.nl/nosnieuwsbuitenland",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=nl&gl=NL&ceid=NL:nl"
                    )
                )
            )
            "pl" -> LanguageNewsSourceConfig(
                langCode = "pl",
                primarySourceName = "Wirtualna Polska • Google Wiadomości",
                secondarySourceName = "Google News PL",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://wiadomosci.wp.pl/rss.xml",
                        "https://news.google.com/rss?hl=pl&gl=PL&ceid=PL:pl"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=pl&gl=PL&ceid=PL:pl"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=pl&gl=PL&ceid=PL:pl"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=pl&gl=PL&ceid=PL:pl"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=pl&gl=PL&ceid=PL:pl"
                    )
                )
            )
            "el" -> LanguageNewsSourceConfig(
                langCode = "el",
                primarySourceName = "in.gr • Google Ειδήσεις",
                secondarySourceName = "Google News GR",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.in.gr/feed/",
                        "https://news.google.com/rss?hl=el&gl=GR&ceid=GR:el"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=el&gl=GR&ceid=GR:el"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=el&gl=GR&ceid=GR:el"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=el&gl=GR&ceid=GR:el"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=el&gl=GR&ceid=GR:el"
                    )
                )
            )
            "id" -> LanguageNewsSourceConfig(
                langCode = "id",
                primarySourceName = "Antara News • Google Berita",
                secondarySourceName = "Antara",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://news.google.com/rss?hl=id&gl=ID&ceid=ID:id"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=id&gl=ID&ceid=ID:id"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.antaranews.com/rss/ekonomi.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=id&gl=ID&ceid=ID:id"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.antaranews.com/rss/tekno.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=id&gl=ID&ceid=ID:id"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.antaranews.com/rss/dunia.xml",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=id&gl=ID&ceid=ID:id"
                    )
                )
            )
            "vi" -> LanguageNewsSourceConfig(
                langCode = "vi",
                primarySourceName = "VnExpress • Google Tin tức",
                secondarySourceName = "VnExpress",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://vnexpress.net/rss/tin-moi-nhat.rss",
                        "https://news.google.com/rss?hl=vi&gl=VN&ceid=VN:vi"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://vnexpress.net/rss/thoi-su.rss",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=vi&gl=VN&ceid=VN:vi"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://vnexpress.net/rss/kinh-doanh.rss",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=vi&gl=VN&ceid=VN:vi"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://vnexpress.net/rss/khoa-hoc.rss",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=vi&gl=VN&ceid=VN:vi"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://vnexpress.net/rss/the-gioi.rss",
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=vi&gl=VN&ceid=VN:vi"
                    )
                )
            )
            "th" -> LanguageNewsSourceConfig(
                langCode = "th",
                primarySourceName = "Google News ไทย",
                secondarySourceName = "Google News TH",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://news.google.com/rss?hl=th&gl=TH&ceid=TH:th"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=th&gl=TH&ceid=TH:th"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=th&gl=TH&ceid=TH:th"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=th&gl=TH&ceid=TH:th"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=th&gl=TH&ceid=TH:th"
                    )
                )
            )
            "zh" -> LanguageNewsSourceConfig(
                langCode = "zh",
                primarySourceName = "Google 新闻",
                secondarySourceName = "Google News CN",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://news.google.com/rss?hl=zh-CN&gl=CN&ceid=CN:zh-Hans"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=zh-CN&gl=CN&ceid=CN:zh-Hans"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=zh-CN&gl=CN&ceid=CN:zh-Hans"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=zh-CN&gl=CN&ceid=CN:zh-Hans"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=zh-CN&gl=CN&ceid=CN:zh-Hans"
                    )
                )
            )
            "ja" -> LanguageNewsSourceConfig(
                langCode = "ja",
                primarySourceName = "NHK • Google ニュース",
                secondarySourceName = "NHK",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www3.nhk.or.jp/rss/news/cat0.xml",
                        "https://news.google.com/rss?hl=ja&gl=JP&ceid=JP:ja"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www3.nhk.or.jp/rss/news/cat4.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=ja&gl=JP&ceid=JP:ja"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www3.nhk.or.jp/rss/news/cat5.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ja&gl=JP&ceid=JP:ja"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www3.nhk.or.jp/rss/news/cat3.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=ja&gl=JP&ceid=JP:ja"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=ja&gl=JP&ceid=JP:ja"
                    )
                )
            )
            "ko" -> LanguageNewsSourceConfig(
                langCode = "ko",
                primarySourceName = "연합뉴스 • Google 뉴스",
                secondarySourceName = "연합뉴스",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.yna.co.kr/rss/politics.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=ko&gl=KR&ceid=KR:ko"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.yna.co.kr/rss/economy.xml",
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ko&gl=KR&ceid=KR:ko"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.yna.co.kr/rss/industry.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=ko&gl=KR&ceid=KR:ko"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=ko&gl=KR&ceid=KR:ko"
                    )
                )
            )
            "hi" -> LanguageNewsSourceConfig(
                langCode = "hi",
                primarySourceName = "Aaj Tak • Google समाचार",
                secondarySourceName = "Aaj Tak",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.aajtak.in/rssfeeds/?id=home",
                        "https://news.google.com/rss?hl=hi&gl=IN&ceid=IN:hi"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=hi&gl=IN&ceid=IN:hi"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=hi&gl=IN&ceid=IN:hi"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=hi&gl=IN&ceid=IN:hi"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://news.google.com/rss/headlines/section/topic/WORLD?hl=hi&gl=IN&ceid=IN:hi"
                    )
                )
            )
            "ur" -> LanguageNewsSourceConfig(
                langCode = "ur",
                primarySourceName = "ایکسپریس نیوز • BBC اردو",
                secondarySourceName = "Express News",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.express.pk/feed/",
                        "https://feeds.bbci.co.uk/urdu/rss.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.express.pk/pakistan/feed/"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.express.pk/business/feed/"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.express.pk/science/feed/"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.express.pk/world/feed/"
                    )
                )
            )
            "fa" -> LanguageNewsSourceConfig(
                langCode = "fa",
                primarySourceName = "RFI فارسی • ایسنا • BBC فارسی",
                secondarySourceName = "ایندیپندنت فارسی",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://www.rfi.fr/fa/rss",
                        "https://feeds.bbci.co.uk/persian/rss.xml"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://www.rfi.fr/fa/%D8%A7%DB%8C%D8%B1%D8%A7%D9%86/rss",
                        "https://www.isna.ir/rss/tp/14"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://www.rfi.fr/fa/%D8%A7%D9%82%D8%AA%D8%B5%D8%A7%D8%AF/rss",
                        "https://www.isna.ir/rss/tp/34"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://www.isna.ir/rss/tp/5",
                        "https://www.independentpersian.com/rss.xml"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://www.isna.ir/rss/tp/17",
                        "https://www.rfi.fr/fa/rss"
                    )
                )
            )
            else -> LanguageNewsSourceConfig(
                langCode = "en",
                primarySourceName = "BBC News • Al Jazeera • France 24",
                secondarySourceName = "Google News",
                sectionFeeds = mapOf(
                    NewsSectionType.TOP_STORIES to listOf(
                        "https://feeds.bbci.co.uk/news/rss.xml",
                        "https://www.france24.com/en/rss"
                    ),
                    NewsSectionType.POLITICS to listOf(
                        "https://feeds.bbci.co.uk/news/politics/rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/NATION?hl=en-US&gl=US&ceid=US:en"
                    ),
                    NewsSectionType.ECONOMY to listOf(
                        "https://feeds.bbci.co.uk/news/business/rss.xml",
                        "https://www.france24.com/en/business-tech/rss"
                    ),
                    NewsSectionType.TECHNOLOGY to listOf(
                        "https://feeds.bbci.co.uk/news/technology/rss.xml",
                        "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=en-US&gl=US&ceid=US:en"
                    ),
                    NewsSectionType.WORLD to listOf(
                        "https://feeds.bbci.co.uk/news/world/rss.xml",
                        "https://www.aljazeera.com/xml/rss/all.xml"
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
        effLang: String,
        maxItems: Int = 16
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

            while (eventType != XmlPullParser.END_DOCUMENT && results.size < maxItems) {
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
                                val isRedundantDesc = description.equals(cleanTitle, ignoreCase = true) ||
                                    (description.startsWith(cleanTitle, ignoreCase = true) && description.length <= cleanTitle.length + 40)
                                val summaryText = if (description.length >= 25 && !isRedundantDesc) {
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
            "ar" -> "منذ $minutes دقيقة"
            "fa" -> "$minutes دقیقه پیش"
            "ur" -> "$minutes منٹ قبل"
            "fr" -> "il y a $minutes min"
            "es" -> "hace $minutes min"
            "it" -> "$minutes min fa"
            "pt" -> "há $minutes min"
            "tr" -> "$minutes dk önce"
            "de" -> "vor $minutes Min."
            "ru" -> "$minutes мин. назад"
            "nl" -> "$minutes min geleden"
            "pl" -> "$minutes min temu"
            "el" -> "πριν $minutes λεπτά"
            "id" -> "$minutes mnt lalu"
            "vi" -> "$minutes phút trước"
            "th" -> "$minutes นาทีที่แล้ว"
            "zh" -> "${minutes}分钟前"
            "ja" -> "${minutes}分前"
            "ko" -> "${minutes}분 전"
            "hi" -> "$minutes मिनट पहले"
            else -> "$minutes min ago"
        }
    }

    private fun getFallbackSummaryForSection(section: NewsSectionType, effLang: String, title: String): String {
        return when (effLang) {
            "ar" -> when (section) {
                NewsSectionType.TOP_STORIES -> "تغطية إخبارية شاملة ومباشرة لأبرز التطورات والأحداث الجارية وتأثيراتها الإقليمية والدولية."
                NewsSectionType.POLITICS -> "متابعة خاصة للمباحثات الدبلوماسية والقرارات السياسية الرسمية والمواقف الدولية الراهنة."
                NewsSectionType.ECONOMY -> "تحليل معمق لحركة الأسواق العالمية ومؤشرات التضخم وأسعار الفائدة والعملات."
                NewsSectionType.TECHNOLOGY -> "أحدث الابتكارات التقنية وتطورات الذكاء الاصطناعي وقطاع الشركات التكنولوجية الكبرى."
                NewsSectionType.WORLD -> "تقارير ميدانية من مختلف العواصم العالمية حول أبرز القضايا والقمم الدولية."
            }
            "fa" -> when (section) {
                NewsSectionType.TOP_STORIES -> "پوشش زنده و جامع مهم‌ترین تحولات و رویدادهای روز: $title"
                NewsSectionType.POLITICS -> "تحلیل تحولات سیاسی، مذاکرات دیپلماتیک و تصمیمات رسمی: $title"
                NewsSectionType.ECONOMY -> "بررسی بازارهای جهانی، شاخص‌های اقتصادی و تحولات مالی: $title"
                NewsSectionType.TECHNOLOGY -> "جدیدترین اخبار فناوری، هوش مصنوعی و نوآوری‌های دیجیتال: $title"
                NewsSectionType.WORLD -> "گزارش‌های بین‌المللی از تحولات مهم در سراسر جهان: $title"
            }
            "ur" -> when (section) {
                NewsSectionType.TOP_STORIES -> "تازہ ترین اور اہم خبروں کی براہِ راست کوریج اور تجزیہ: $title"
                NewsSectionType.POLITICS -> "سیاسی صورتحال، سفارتی سرگرمیوں اور سرکاری فیصلوں کی رپورٹ: $title"
                NewsSectionType.ECONOMY -> "عالمی منڈیوں، معاشی اشاریوں اور کاروباری خبروں کا جائزہ: $title"
                NewsSectionType.TECHNOLOGY -> "ٹیکنالوجی، مصنوعی ذہانت اور سائنسی ایجادات کی تازہ خبریں: $title"
                NewsSectionType.WORLD -> "دنیا بھر سے اہم بین الاقوامی حالات اور واقعات کی تفصیلات: $title"
            }
            "fr" -> "Actualités en direct : $title."
            "es" -> "Cobertura en vivo y análisis: $title."
            "it" -> "Aggiornamenti in diretta e analisi: $title."
            "pt" -> "Cobertura ao vivo e análise: $title."
            "de" -> "Aktuelle Berichterstattung und Analyse: $title."
            "tr" -> "Canlı gelişmeler ve ayrıntılı analiz: $title."
            "ru" -> "Прямой эфир и подробный обзор: $title."
            "nl" -> "Live nieuws en achtergrond: $title."
            "pl" -> "Relacja na żywo i analiza: $title."
            "el" -> "Ζωντανή κάλυψη και ανάλυση: $title."
            "id" -> "Liputan langsung dan analisis: $title."
            "vi" -> "Tin tức trực tiếp và phân tích: $title."
            "th" -> "รายงานสดและบทวิเคราะห์: $title"
            "zh" -> "实时新闻报道与深度解析：$title"
            "ja" -> "最新ニュース速報と解説：$title"
            "ko" -> "실시간 뉴스 속보 및 분석: $title"
            "hi" -> "ताज़ा समाचार कवरेज और विश्लेषण: $title"
            else -> "Live coverage and in-depth analysis on: $title."
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

    internal fun buildBaselineState(effLang: String): NewsCenterState {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val sourceConfig = getBestSourceConfigForLanguage(effLang)
        val strings = getLocalizedStrings(effLang)

        if (effLang == "ar") {
            val topStoriesAr = listOf(
                NewsArticleItem(
                    id = "ar_top_1",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "قمة دولية طارئة لبحث تعزيز الاستقرار وتأمين سلاسل الإمداد العالمية",
                    summary = "انطلقت اليوم أعمال القمة بمشاركة قادة ورؤساء وفود لوضع خارطة طريق شاملة لدعم الاستقرار وتنشيط الممرات التجارية.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 6),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_top_2",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "مبادرة جديدة لتوسيع مشاريع الطاقة النظيفة والتحول الرقمي",
                    summary = "أعلنت عدة دول عن شراكة استثمارية طويلة الأمد لربط الشبكات الكهربائية النظيفة وتحديث البنية التحتية الرقمية.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 12),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 1),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_top_3",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "ارتفاع قياسي في حركة النقل الجوي والسياحة الدولية خلال الربع الحالي",
                    summary = "كشفت تقارير النقل المدني عن نمو ملموس في الرحلات العابرة للقارات وزيادة السعة الاستيعابية للمطارات الرئيسية.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 18),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 2),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "ar_top_4",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "تطوير معايير موحدة لجودة الخدمات الصحية والاستجابة السريعة",
                    summary = "أطلقت هيئات دولية برنامجاً مشتركاً لدعم المستشفيات بالتقنيات الحديثة وتبادل الخبرات الطبية عن بُعد.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 24),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 3),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "ar_top_5",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "تعاون جامعي واسع لتدريب الكفاءات الشابة في مجالات البحث العلمي",
                    summary = "اتفاقيات أكاديمية جديدة توفر منحاً بحثية متقدمة وتبادلاً طلابياً في تخصصات الهندسة والبيئة.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 30),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 4),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "ar_top_6",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "خطط حكومية لتحديث شبكات النقل العام والقطارات فائقة السرعة",
                    summary = "مشروعات استراتيجية جديدة تربط المدن الكبرى بخطوط سكك حديدية كهربائية صديقة للبيئة.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 36),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 5),
                    visualTheme = "central_bank"
                )
            )

            val politicsAr = listOf(
                NewsArticleItem(
                    id = "ar_pol_1",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "مباحثات دبلوماسية رفيعة المستوى لتعزيز التعاون الأمني والإقليمي",
                    summary = "وزراء الخارجية يناقشون في ختام اجتماعاتهم آليات التنسيق السياسي ودعم مسارات التهدئة والحوار.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 8),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_pol_2",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "البرلمان يناقش حزمة تشريعات جديدة لتطوير الحوكمة والشفافية الإدارية",
                    summary = "اللجان البرلمانية ترفع توصياتها النهائية حول مشاريع القوانين المنظمة للعمل البلدي والرقابة المالية.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 14),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 1),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_pol_3",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "توقيع اتفاقية شراكة استراتيجية وتعاون قضائي بين عدة عواصم",
                    summary = "الوثيقة الجديدة تهدف إلى تسريع تبادل المعلومات القانونية وتسهيل الإجراءات الدبلوماسية والقنصلية.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 20),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 2),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_pol_4",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "جولة مشاورات رسمية لبحث ملفات الحدود البحرية وحماية الممرات",
                    summary = "وفود فنية وقانونية تستكمل صياغة التفاهمات المشتركة لضمان حرية الملاحة والتعاون المشترك.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 26),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 3),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_pol_5",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "بيان مشترك يؤكد على أهمية الحلول السلمية ودعم جهود الإغاثة",
                    summary = "شددت الأطراف المشاركة على ضرورة تنسيق المساعدات الإنسانية وضمان وصولها للمناطق المتضررة.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 32),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 4),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_pol_6",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "تشكيل لجنة وزارية مشتركة لمتابعة تنفيذ اتفاقيات التجارة الحرة",
                    summary = "اللجنة ستعقد اجتماعات دورية لرفع المعوقات الجمركية وتنشيط التبادل التجاري البيني.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 38),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 5),
                    visualTheme = "central_bank"
                )
            )

            val economyAr = listOf(
                NewsArticleItem(
                    id = "ar_eco_1",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "انتعاش الأسواق العالمية مع تباطؤ التضخم بوتيرة أسرع من التوقعات",
                    summary = "سجلت المؤشرات العالمية مستويات قياسية بعد أن كشفت بيانات مؤشر أسعار المستهلكين عن تراجع حاد في التضخم.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 7),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 0),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "ar_eco_2",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "البنك المركزي يقرر تثبيت أسعار الفائدة لدعم استقرار النمو الاقتصادي",
                    summary = "أكد صناع السياسة النقدية استمرار مراقبة مؤشرات الأسعار وسوق العمل قبل اتخاذ أي خطوات جديدة.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 13),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 1),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "ar_eco_3",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "ارتفاع احتياطيات النقد الأجنبي مع نمو الصادرات غير النفطية",
                    summary = "أظهرت البيانات الرسمية تحسناً ملحوظاً في الميزان التجاري بفضل زيادة الصادرات الصناعية والزراعية.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 19),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 2),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "ar_eco_4",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "توقعات بتعافي أسعار المعادن الأساسية مع ارتفاع الطلب الصناعي",
                    summary = "محللو السلع يتوقعون طلباً قوياً على النحاس والليثيوم مدفوعاً بمشاريع الطاقة المتجددة والسيارات الكهربائية.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 25),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 3),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "ar_eco_5",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "صناديق استثمارية توسع محفظتها في قطاع التكنولوجيا المالية والمدفوعات",
                    summary = "جولات تمويل جديدة تدعم الشركات الناشئة المتخصصة في حلول الدفع الرقمي والخدمات المصرفية السحابية.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 31),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 4),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "ar_eco_6",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "تراجع تكاليف الشحن البحري يعزز مرونة التجارة الإلكترونية العالمية",
                    summary = "مؤشرات الموانئ تشير إلى تحسن وتيرة تفريغ الحاويات وانخفاض أسعار التأمين على خطوط الشحن الرئيسية.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 37),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 5),
                    visualTheme = "central_bank"
                )
            )

            val techAr = listOf(
                NewsArticleItem(
                    id = "ar_tech_1",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "الكشف عن جيل جديد من نماذج الذكاء الاصطناعي فائقة الكفاءة",
                    summary = "المعمارية الجديدة توفر معالجة فورية للنصوص والصوت والصورة باستهلاك طاقة أقل داخل مراكز البيانات.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 9),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 0),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "ar_tech_2",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "إطلاق معالجات متطورة للحوسبة السحابية بدقة تصنيع غير مسبوقة",
                    summary = "رقائق السيليكون الجديدة ترفع سرعة تدريب النماذج الذكية وتقلل زمن الاستجابة في الأجهزة المحمولة.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 15),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 1),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "ar_tech_3",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "ابتكار بطاريات صلبة للسيارات الكهربائية تُشحن خلال عشر دقائق",
                    summary = "نجح باحثون في تطوير إلكتروليت صلب يضاعف مدى القيادة ويحافظ على الأداء في درجات الحرارة القصوى.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 21),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 2),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "ar_tech_4",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "تحديثات أمنية واسعة لتعزيز حماية الشبكات السحابية والهواتف الذكية",
                    summary = "شركات الأمن السيبراني تعتمد التشفير المقاوم للحوسبة الكمية لحماية بيانات المستخدمين والمؤسسات.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 27),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 3),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "ar_tech_5",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "إطلاق أقمار صناعية جديدة لتوسيع تغطية الإنترنت الفضائي عالي السرعة",
                    summary = "المجموعة المدارية الجديدة تستهدف توفير اتصال مستقر للمناطق النائية والطائرات والسفن حول العالم.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 33),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 4),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "ar_tech_6",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "ثورة في تقنيات الشاشات الذكية والتلفزيونات فائقة التباين",
                    summary = "لوحات العرض الجديدة تجمع بين السطوع العالي وانخفاض استهلاك الكهرباء مع معالجة حركة بدقة 4K.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 39),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 5),
                    visualTheme = "ai_datacenter"
                )
            )

            val worldAr = listOf(
                NewsArticleItem(
                    id = "ar_wld_1",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "التوصل إلى اتفاق مناخي جديد خلال القمة العالمية للطاقة المستدامة",
                    summary = "اتفق القادة على تسريع الاستثمارات في مشاريع الطاقة النظيفة وخفض الانبعاثات الكربونية خلال العقد القادم.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 10),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 0),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_wld_2",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "مؤتمر دولي للأمن الغذائي يطلق صندوقاً لدعم الزراعة الذكية مناخياً",
                    summary = "برنامج المساعدات يهدف إلى تطوير بذور مقاومة للجفاف وتحديث أنظمة الري في عشرات الدول النامية.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 16),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 1),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_wld_3",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "اكتشافات أثرية نادرة تلقي الضوء على طرق التجارة القديمة في المتوسط",
                    summary = "بعثة دولية تكشف عن ميناء غارق ومجموعة نقود تاريخية محفوظة بحالة ممتازة تعود لعدة قرون.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 22),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 2),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "ar_wld_4",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "المدن الأوروبية والآسيوية تتوسع في إنشاء المساحات الخضراء للحد من الحرارة",
                    summary = "بلديات المدن الكبرى تخصص مليارات الدولارات لزراعة الغابات الحضرية وتبريد الأحياء السكنية.",
                    sourceName = "BBC عربي",
                    timeAgo = getRelativeTimeLabel(effLang, 28),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 3),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_wld_5",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "بعثات استكشاف المحيطات ترصد أنظمة بيئية بحرية لم تُكتشف من قبل",
                    summary = "مركبات أعماق البحار توثق أنواعاً جديدة من الشعاب المرجانية العميقة التي تساهم في توازن التنوع البيولوجي.",
                    sourceName = "فرانس 24",
                    timeAgo = getRelativeTimeLabel(effLang, 34),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 4),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "ar_wld_6",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "منظمات دولية تدعو لحماية التراث الثقافي الرقمي والأرشيفات التاريخية",
                    summary = "مشروع عالمي جديد لحفظ المخطوطات والوثائق النادرة عبر مسح ضوئي عالي الدقة متاح للباحثين مجاناً.",
                    sourceName = "سكاي نيوز عربية",
                    timeAgo = getRelativeTimeLabel(effLang, 40),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 5),
                    visualTheme = "diplomacy_summit"
                )
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
                    "البنك المركزي يقرر تثبيت أسعار الفائدة لدعم استقرار النمو",
                    "التوصل إلى اتفاق مناخي جديد خلال القمة العالمية للطاقة",
                    "الكشف عن جيل جديد من نماذج الذكاء الاصطناعي فائقة الكفاءة",
                    "قمة دولية طارئة لتأمين سلاسل الإمداد والممرات التجارية"
                ),
                updatedTime = timeFormat.format(Date()),
                fetchedAtMillis = 0L,
                isLoading = false
            )
        } else {
            val topStoriesEn = listOf(
                NewsArticleItem(
                    id = "${effLang}_top_1",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "Emergency Global Summit Convenes to Strengthen Supply Chain Resilience",
                    summary = "International leaders met today to coordinate cross-border logistics, energy corridors, and long-term infrastructure resilience.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 6),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "${effLang}_top_2",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "Clean Grid Partnership Expands Cross-Border Renewable Power Links",
                    summary = "Energy ministries signed a multi-year investment pact connecting offshore wind hubs and next-generation grid storage.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 12),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 1),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_top_3",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "International Aviation Traffic Surpasses Pre-Pandemic Peak Volumes",
                    summary = "Civil aviation authorities reported sustained demand across long-haul corridors alongside major terminal upgrades.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 18),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 2),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "${effLang}_top_4",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "Global Health Coalition Launches Rapid Telemedicine Network",
                    summary = "Regional hospitals will share real-time diagnostics and specialist training across underserved rural clinics.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 24),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 3),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "${effLang}_top_5",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "Universities Announce Joint Fellowships in Climate & Materials Science",
                    summary = "New international grants will fund collaborative laboratories working on zero-carbon industrial manufacturing.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 30),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 4),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "${effLang}_top_6",
                    section = NewsSectionType.TOP_STORIES,
                    categoryLabel = strings.sectionTopStories,
                    headline = "High-Speed Electric Rail Expansion Links Major Metropolitan Corridors",
                    summary = "Transport planners unveiled new electrified intercity routes designed to cut regional travel times in half.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 36),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TOP_STORIES, 5),
                    visualTheme = "central_bank"
                )
            )

            val politicsEn = listOf(
                NewsArticleItem(
                    id = "${effLang}_pol_1",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Diplomatic Talks Advance on Regional Trade and Security Framework",
                    summary = "Foreign ministers finalized cross-border customs protocols and strategic dialogue mechanisms after three days of talks.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 8),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 0),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "${effLang}_pol_2",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Lawmakers Debate Landmark Public Transparency and Digital Governance Bill",
                    summary = "Parliamentary committees advanced bipartisan proposals to modernize public procurement and digital citizen services.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 14),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 1),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "${effLang}_pol_3",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Capitals Sign Bilateral Accord on Maritime Corridors and Port Security",
                    summary = "Legal and maritime delegations agreed on joint coast guard coordination and streamlined vessel inspection rules.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 20),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 2),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "${effLang}_pol_4",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Municipal Leaders Urge National Funding for Urban Housing Modernization",
                    summary = "Mayors from major cities presented a shared infrastructure plan targeting affordable transit-oriented neighborhoods.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 26),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 3),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_pol_5",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Joint Ministerial Council Sets Schedule for Tariff Reduction Review",
                    summary = "Trade envoys scheduled quarterly working sessions to remove technical barriers on agricultural and industrial goods.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 32),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 4),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "${effLang}_pol_6",
                    section = NewsSectionType.POLITICS,
                    categoryLabel = strings.sectionPolitics,
                    headline = "Election Oversight Panel Publishes Updated Cybersecurity Standards",
                    summary = "Independent regulators released audit guidelines to safeguard voter registries and public tally infrastructure.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 38),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.POLITICS, 5),
                    visualTheme = "diplomacy_summit"
                )
            )

            val economyEn = listOf(
                NewsArticleItem(
                    id = "${effLang}_eco_1",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Global Equities Rally as Core Inflation Cools Faster Than Forecast",
                    summary = "Benchmark stock indexes reached fresh highs after consumer price reports pointed to easing services inflation.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 7),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 0),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "${effLang}_eco_2",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Central Bank Holds Benchmark Interest Rates Steady Amid Resilient Growth",
                    summary = "Monetary policymakers signaled a data-dependent stance as labor markets remain balanced and credit conditions stabilize.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 13),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 1),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "${effLang}_eco_3",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Industrial Metals Gain Momentum on Clean Energy Manufacturing Demand",
                    summary = "Copper and aluminum futures climbed as factory orders accelerated across grid equipment and electric vehicle plants.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 19),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 2),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "${effLang}_eco_4",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Maritime Freight Rates Ease as Container Port Turnaround Improves",
                    summary = "Global shipping indexes recorded steady capacity normalization along trans-Pacific and European maritime routes.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 25),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 3),
                    visualTheme = "central_bank"
                ),
                NewsArticleItem(
                    id = "${effLang}_eco_5",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Venture Capital Activity Rebounds in Fintech and Cloud Infrastructure",
                    summary = "Institutional investors expanded late-stage allocations into automated treasury tools and enterprise payment rails.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 31),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 4),
                    visualTheme = "trading_floor"
                ),
                NewsArticleItem(
                    id = "${effLang}_eco_6",
                    section = NewsSectionType.ECONOMY,
                    categoryLabel = strings.sectionEconomy,
                    headline = "Export Orders Lift Manufacturing Sentiment Across Emerging Markets",
                    summary = "Purchasing managers surveys showed rising factory output and stronger hiring intentions heading into next quarter.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 37),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.ECONOMY, 5),
                    visualTheme = "central_bank"
                )
            )

            val techEn = listOf(
                NewsArticleItem(
                    id = "${effLang}_tech_1",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Breakthrough Multimodal AI Architecture Cuts Data Center Power Use",
                    summary = "Next-generation reasoning models deliver real-time inference with substantially lower energy consumption.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 9),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 0),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "${effLang}_tech_2",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Next-Generation Quantum Processor Achieves Fault-Tolerant Milestone",
                    summary = "Researchers demonstrated logical qubit error correction capable of sustaining complex molecular simulations.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 15),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 1),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "${effLang}_tech_3",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Solid-State EV Battery Prototype Reaches 80% Charge in Ten Minutes",
                    summary = "Materials scientists unveiled a ceramic electrolyte cell engineered for high thermal stability and longevity.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 21),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 2),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "${effLang}_tech_4",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Open Standards Group Finalizes Post-Quantum Encryption Suite",
                    summary = "Cloud providers and device makers began rolling out upgraded cryptographic protocols for consumer hardware.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 27),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 3),
                    visualTheme = "ai_datacenter"
                ),
                NewsArticleItem(
                    id = "${effLang}_tech_5",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Low-Earth Orbit Constellation Expands High-Speed Rural Broadband",
                    summary = "Newly deployed laser-linked satellites reduce latency across maritime, aviation, and remote regional networks.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 33),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 4),
                    visualTheme = "cyber_chip"
                ),
                NewsArticleItem(
                    id = "${effLang}_tech_6",
                    section = NewsSectionType.TECHNOLOGY,
                    categoryLabel = strings.sectionTechnology,
                    headline = "Next-Gen MicroLED Smart TV Panels Double Peak Brightness Efficiency",
                    summary = "Display engineers introduced ultra-thin television backplanes pairing deep contrast with lower power draw.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 39),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.TECHNOLOGY, 5),
                    visualTheme = "ai_datacenter"
                )
            )

            val worldEn = listOf(
                NewsArticleItem(
                    id = "${effLang}_wld_1",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "New Climate Accord Reached at Global Sustainable Energy Summit",
                    summary = "Delegations pledged accelerated funding for clean power grids and coastal resilience over the coming decade.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 10),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 0),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_wld_2",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "International Food Security Forum Backs Drought-Resilient Farming",
                    summary = "Agricultural agencies committed resources to precision irrigation and climate-adapted grain varieties.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 16),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 1),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_wld_3",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "Deep-Sea Expedition Maps Previously Uncharted Coral Ecosystems",
                    summary = "Autonomous submersibles documented thriving deep-water reefs supporting diverse pelagic marine life.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 22),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 2),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_wld_4",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "Major Cities Expand Urban Tree Canopies to Curb Summer Heat",
                    summary = "Metropolitan councils across Europe and Asia launched green-corridor programs to cool dense districts.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 28),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 3),
                    visualTheme = "global_climate"
                ),
                NewsArticleItem(
                    id = "${effLang}_wld_5",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "Archaeologists Uncover Ancient Mediterranean Harbor Artifacts",
                    summary = "An international dive team preserved ceramics and trade records illuminating centuries-old maritime routes.",
                    sourceName = sourceConfig.primarySourceName.substringBefore("•").trim(),
                    timeAgo = getRelativeTimeLabel(effLang, 34),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 4),
                    visualTheme = "diplomacy_summit"
                ),
                NewsArticleItem(
                    id = "${effLang}_wld_6",
                    section = NewsSectionType.WORLD,
                    categoryLabel = strings.sectionWorld,
                    headline = "Global Cultural Coalition Digitizes Rare Historical Archives",
                    summary = "Libraries across four continents opened free high-resolution access to fragile manuscripts and maps.",
                    sourceName = sourceConfig.secondarySourceName,
                    timeAgo = getRelativeTimeLabel(effLang, 40),
                    imageUrl = getFallbackPhotoForSection(NewsSectionType.WORLD, 5),
                    visualTheme = "diplomacy_summit"
                )
            )

            return NewsCenterState(
                languageCode = effLang,
                activeSourceName = sourceConfig.primarySourceName,
                articlesBySection = mapOf(
                    NewsSectionType.TOP_STORIES to topStoriesEn,
                    NewsSectionType.POLITICS to politicsEn,
                    NewsSectionType.ECONOMY to economyEn,
                    NewsSectionType.TECHNOLOGY to techEn,
                    NewsSectionType.WORLD to worldEn
                ),
                breakingTickerItems = listOf(
                    "Central bank holds interest rates steady amid resilient growth",
                    "New climate agreement reached at global summit",
                    "Breakthrough AI architecture cuts data center power use",
                    "Global equities rally as core inflation cools faster than forecast"
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
            "ar" -> NewsLocalizedStrings(
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
            "fa" -> NewsLocalizedStrings(
                newsCenterTitle = "مرکز خبر",
                liveBadge = "زنده",
                sectionsHeader = "بخش‌ها",
                sectionTopStories = "مهم‌ترین اخبار",
                sectionPolitics = "سیاست",
                sectionEconomy = "اقتصاد",
                sectionTechnology = "فناوری",
                sectionWorld = "جهان",
                upNextHeader = "خبر بعدی",
                upNextPrefix = "بعدی: ",
                updatedPrefix = "بروزرسانی ",
                breakingBadge = "فوری",
                sourceLabel = "منبع خودکار: ",
                refreshAction = "بروزرسانی اخبار",
                backToHomeHint = "بازگشت به خانه"
            )
            "ur" -> NewsLocalizedStrings(
                newsCenterTitle = "نیوز سینٹر",
                liveBadge = "لائیو",
                sectionsHeader = "شعبہ جات",
                sectionTopStories = "اہم خبریں",
                sectionPolitics = "سیاست",
                sectionEconomy = "معیشت",
                sectionTechnology = "ٹیکنالوجی",
                sectionWorld = "دنیا",
                upNextHeader = "اگلی خبر",
                upNextPrefix = "اگلا: ",
                updatedPrefix = "اپڈیٹ ",
                breakingBadge = "بریکنگ",
                sourceLabel = "خودکار ذریعہ: ",
                refreshAction = "خبریں تازہ کریں",
                backToHomeHint = "ہوم پر واپس"
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
