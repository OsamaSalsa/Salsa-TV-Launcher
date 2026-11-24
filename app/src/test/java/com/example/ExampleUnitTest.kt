package com.example

import com.example.data.local.LauncherPreferences
import com.example.data.update.AppUpdateManager
import com.example.service.DnsBlockerService
import com.example.ui.common.YOUTUBE_TV_USER_AGENT
import com.example.util.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `default web presets contain youtube tv, google, and four web games`() {
        val presets = LauncherPreferences.PRESET_WEB_BOOKMARKS
        assertEquals(6, presets.size)
        val youtube = presets.first { it.id == "preset_youtube" }
        assertEquals("https://www.youtube.com/tv", youtube.url)
        val google = presets.first { it.id == "preset_google" }
        assertEquals("https://www.google.com", google.url)
        assertEquals(
            setOf("preset_youtube", "preset_google", "preset_crazygames", "preset_poki", "preset_playhop", "preset_easygames"),
            presets.map { it.id }.toSet()
        )
    }

    @Test
    fun `youtube tv user agent targets smart tv browser`() {
        assertTrue(YOUTUBE_TV_USER_AGENT.contains("SMART-TV"))
    }

    @Test
    fun `app build version is 3_0 with versionCode 8 and supports auto update checks`() {
        assertEquals("3.0", BuildConfig.VERSION_NAME)
        assertEquals(8, BuildConfig.VERSION_CODE)
        assertEquals("ps.salsa.tvlauncher", BuildConfig.APPLICATION_ID)
        // Older v2.1 (code 7) upgrades to v3.0 (code 8)
        assertTrue(AppUpdateManager.isRemoteNewer(8, "3.0", 7, "2.1"))
        // Current v3.0 (code 8) upgrades to future v3.1 (code 9)
        assertTrue(AppUpdateManager.isRemoteNewer(9, "3.1", 8, "3.0"))
        // Same version does not trigger update
        assertFalse(AppUpdateManager.isRemoteNewer(8, "3.0", 8, "3.0"))
        // Lower versionCode never triggers downgrade
        assertFalse(AppUpdateManager.isRemoteNewer(7, "2.1", 8, "3.0"))
        assertFalse(AppUpdateManager.isRemoteNewer(7, "9.9", 8, "3.0"))
    }

    @Test
    fun `app update manager compares semantic versions accurately`() {
        assertTrue(AppUpdateManager.compareVersions("1.4", "1.3") > 0)
        assertTrue(AppUpdateManager.compareVersions("v1.10", "v1.4") > 0)
        assertEquals(0, AppUpdateManager.compareVersions("v1.4", "1.4"))
        assertTrue(AppUpdateManager.compareVersions("1.3.9", "1.4") < 0)
    }

    @Test
    fun `app update manager converts github blob urls to raw`() {
        val raw = AppUpdateManager.normalizeDownloadUrl(
            "https://github.com/OsamaSalsa/Salsa-TV-Launcher/blob/main/salsa-tv-launcher.apk"
        )
        assertEquals(
            "https://raw.githubusercontent.com/OsamaSalsa/Salsa-TV-Launcher/main/salsa-tv-launcher.apk",
            raw
        )
    }

    @Test
    fun `dns blocker configures distinct ipv4 and ipv6 adguard profiles`() {
        assertEquals(4, DnsBlockerService.ADS_DNS_SERVERS.size)
        assertEquals(4, DnsBlockerService.FAMILY_DNS_SERVERS.size)
        assertTrue(DnsBlockerService.ADS_DNS_SERVERS.contains("94.140.14.14"))
        assertTrue(DnsBlockerService.FAMILY_DNS_SERVERS.contains("94.140.14.15"))
    }

    @Test
    fun `locale helper identifies rtl and sanitizes invisible marks for cairo font`() {
        assertTrue(LocaleHelper.isRtlLanguage("ar_rtl"))
        assertTrue(LocaleHelper.isRtlLanguage("fa"))
        assertTrue(LocaleHelper.isRtlLanguage("ur"))
        assertFalse(LocaleHelper.isRtlLanguage("en"))
        assertEquals("ar", LocaleHelper.getEffectiveLanguage("ar_rtl"))
        assertEquals("id", LocaleHelper.getEffectiveLanguage("in"))

        val dirty = "\u061Cالأحد\u202F2026\u200F"
        assertEquals("الأحد 2026", LocaleHelper.sanitizeForCairoFont(dirty))
    }

    @Test
    fun `extract youtube video id handles standard, short, embed, live, and raw ids`() {
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId("https://www.youtube.com/watch?v=EG_g7BFaL5s&t=12s"))
        assertEquals("TtLeec_DR-Y", com.example.ui.home.extractYouTubeVideoId("https://youtu.be/TtLeec_DR-Y?si=abc"))
        assertEquals("8xXHeRnLwNQ", com.example.ui.home.extractYouTubeVideoId("https://www.youtube.com/embed/8xXHeRnLwNQ?autoplay=1"))
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId("EG_g7BFaL5s"))
        assertEquals("EG_g7BFaL5s", com.example.ui.home.extractYouTubeVideoId(""))
    }

    @Test
    fun `broadcast text parser splits multi-line text and falls back to defaults`() {
        val parsed = com.example.ui.home.parseBroadcastLines(
            rawText = "  السطر الأول  \r\n\nالسطر الثاني\n   \nالسطر الثالث",
            fallbackDefaultText = "افتراضي 1\nافتراضي 2"
        )
        assertEquals(listOf("السطر الأول", "السطر الثاني", "السطر الثالث"), parsed)

        val fallback = com.example.ui.home.parseBroadcastLines(
            rawText = "   \n  ",
            fallbackDefaultText = "افتراضي 1\nافتراضي 2"
        )
        assertEquals(listOf("افتراضي 1", "افتراضي 2"), fallback)

        assertEquals("", com.example.ui.home.resolveAmbientMusicUrl("off", "content://audio/1"))
        assertEquals("content://audio/1", com.example.ui.home.resolveAmbientMusicUrl("custom_file", "content://audio/1"))
        assertTrue(com.example.ui.home.resolveAmbientMusicUrl("lofi_chill", "").startsWith("https://"))
    }

    @Test
    fun `dashboard screensaver cycle gives news one minute and advances news section every football wrap`() {
        assertEquals(60_000L, com.example.ui.home.DashboardScreensaverSlide.NEWS.durationMs)
        assertEquals(10_000L, com.example.ui.home.DashboardScreensaverSlide.WEATHER.durationMs)
        assertEquals(10_000L, com.example.ui.home.DashboardScreensaverSlide.ECONOMIC.durationMs)
        assertEquals(10_000L, com.example.ui.home.DashboardScreensaverSlide.FOOTBALL.durationMs)

        com.example.ui.home.DashboardScreensaverCycleTracker.resetForTest()
        assertEquals(com.example.data.news.NewsSectionType.TOP_STORIES, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(0))
        assertEquals(com.example.data.news.NewsSectionType.POLITICS, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(com.example.ui.home.DashboardScreensaverCycleTracker.advanceNextNewsCycle()))
        assertEquals(com.example.data.news.NewsSectionType.ECONOMY, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(com.example.ui.home.DashboardScreensaverCycleTracker.advanceNextNewsCycle()))
        assertEquals(com.example.data.news.NewsSectionType.TECHNOLOGY, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(com.example.ui.home.DashboardScreensaverCycleTracker.advanceNextNewsCycle()))
        assertEquals(com.example.data.news.NewsSectionType.WORLD, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(com.example.ui.home.DashboardScreensaverCycleTracker.advanceNextNewsCycle()))
        assertEquals(com.example.data.news.NewsSectionType.TOP_STORIES, com.example.ui.home.DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(com.example.ui.home.DashboardScreensaverCycleTracker.advanceNextNewsCycle()))
        assertEquals(2, com.example.ui.home.DashboardScreensaverCycleTracker.resolveStoryOffsetForCycle(5))
        com.example.ui.home.DashboardScreensaverCycleTracker.resetForTest()
    }

    @Test
    fun `news repository provides dedicated source configs and distinct section feeds for all 21 supported languages`() {
        val effectiveLanguages = LocaleHelper.SUPPORTED_LANGUAGES
            .map { LocaleHelper.getEffectiveLanguage(it) }
            .distinct()
        assertEquals(21, effectiveLanguages.size)

        for (lang in effectiveLanguages) {
            val config = com.example.data.news.NewsRepository.getBestSourceConfigForLanguage(lang)
            assertEquals("Expected dedicated langCode for $lang", lang, config.langCode)
            assertEquals(5, config.sectionFeeds.size)

            val feedSets = com.example.data.news.NewsSectionType.entries.map { section ->
                val feeds = config.sectionFeeds[section].orEmpty()
                assertTrue("Expected non-empty feeds for $lang/$section", feeds.isNotEmpty())
                feeds.first()
            }
            // Every language must have distinct primary feeds across all 5 sections
            assertEquals("Expected 5 distinct primary section feeds for $lang", 5, feedSets.toSet().size)
        }
    }

    @Test
    fun `news repository deduplicates overlapping headlines across sections and keeps baseline sections distinct`() {
        val sharedHeadline = "Global Summit Reaches Historic Climate Agreement - Reuters"
        val uniqueHeadlineA = "Local Parliament Approves Annual Infrastructure Budget"
        val uniqueHeadlineB = "Stock Markets Surge Following Central Bank Guidance"
        val uniqueHeadlineC = "Next-Generation AI Chip Achieves Record Efficiency"
        val uniqueHeadlineD = "United Nations Launches Clean Water Initiative"

        fun makeItem(section: com.example.data.news.NewsSectionType, idx: Int, title: String) =
            com.example.data.news.NewsArticleItem(
                id = "${section.id}_live_$idx",
                section = section,
                categoryLabel = section.name,
                headline = title,
                summary = "Summary for $title",
                sourceName = "Test Source",
                timeAgo = "6 min ago",
                imageUrl = "https://example.com/img.jpg",
                visualTheme = "trading_floor"
            )

        val candidates = mapOf(
            com.example.data.news.NewsSectionType.TOP_STORIES to listOf(
                makeItem(com.example.data.news.NewsSectionType.TOP_STORIES, 0, sharedHeadline)
            ),
            com.example.data.news.NewsSectionType.POLITICS to listOf(
                makeItem(com.example.data.news.NewsSectionType.POLITICS, 0, sharedHeadline),
                makeItem(com.example.data.news.NewsSectionType.POLITICS, 1, uniqueHeadlineA)
            ),
            com.example.data.news.NewsSectionType.ECONOMY to listOf(
                makeItem(com.example.data.news.NewsSectionType.ECONOMY, 0, sharedHeadline),
                makeItem(com.example.data.news.NewsSectionType.ECONOMY, 1, uniqueHeadlineB)
            ),
            com.example.data.news.NewsSectionType.TECHNOLOGY to listOf(
                makeItem(com.example.data.news.NewsSectionType.TECHNOLOGY, 0, uniqueHeadlineC)
            ),
            com.example.data.news.NewsSectionType.WORLD to listOf(
                makeItem(com.example.data.news.NewsSectionType.WORLD, 0, sharedHeadline),
                makeItem(com.example.data.news.NewsSectionType.WORLD, 1, uniqueHeadlineD)
            )
        )

        val baseline = com.example.data.news.NewsRepository.buildBaselineState("en")
        val assigned = com.example.data.news.NewsRepository.assignDistinctSectionArticles(
            candidatesBySection = candidates,
            baselineBySection = baseline.articlesBySection,
            strings = com.example.data.news.NewsRepository.getLocalizedStrings("en"),
            effLang = "en"
        )

        val allAssignedHeadlines = assigned.values.flatten().map {
            com.example.data.news.NewsRepository.normalizeHeadlineKey(it.headline)
        }
        assertEquals("Headlines across all 5 sections must be unique", allAssignedHeadlines.size, allAssignedHeadlines.toSet().size)
        assertEquals(uniqueHeadlineA, assigned[com.example.data.news.NewsSectionType.POLITICS]?.firstOrNull()?.headline)
        assertEquals(uniqueHeadlineB, assigned[com.example.data.news.NewsSectionType.ECONOMY]?.firstOrNull()?.headline)
        assertEquals(uniqueHeadlineC, assigned[com.example.data.news.NewsSectionType.TECHNOLOGY]?.firstOrNull()?.headline)
        assertEquals(uniqueHeadlineD, assigned[com.example.data.news.NewsSectionType.WORLD]?.firstOrNull()?.headline)

        for (lang in listOf("ar", "en", "fr", "fa", "ur")) {
            val state = com.example.data.news.NewsRepository.buildBaselineState(lang)
            val headlines = state.articlesBySection.values.flatten().map { it.headline }
            assertEquals("Baseline for $lang must have 30 distinct articles across 5 sections", 30, headlines.toSet().size)
        }
    }

    @Test
    fun `news voice reader builds spoken text without publication date or duplicate headline`() {
        val storyWithDate = com.example.data.news.NewsArticleItem(
            id = "ar_top_1",
            section = com.example.data.news.NewsSectionType.TOP_STORIES,
            categoryLabel = "أهم الأخبار",
            headline = "إطلاق مبادرة اقتصادية جديدة لتعزيز الاستثمار",
            summary = "تتضمن المبادرة حزمة تسهيلات للقطاع الصناعي والتقني • منذ 15 دقيقة • 2026-11-23",
            sourceName = "الجزيرة",
            timeAgo = "منذ 6 دقائق",
            imageUrl = ""
        )
        val spoken = com.example.ui.news.buildSpokenNewsText(storyWithDate)
        assertTrue(spoken.contains("إطلاق مبادرة اقتصادية جديدة لتعزيز الاستثمار"))
        assertTrue(spoken.contains("تتضمن المبادرة حزمة تسهيلات للقطاع الصناعي والتقني"))
        assertFalse("Must not speak timeAgo", spoken.contains("منذ 6 دقائق"))
        assertFalse("Must not speak embedded relative time", spoken.contains("منذ 15 دقيقة"))
        assertFalse("Must not speak embedded ISO date", spoken.contains("2026-11-23"))

        val storyWithRedundantSummary = com.example.data.news.NewsArticleItem(
            id = "fr_top_1",
            section = com.example.data.news.NewsSectionType.WORLD,
            categoryLabel = "Monde",
            headline = "Accord historique sur le climat",
            summary = "Actualités en direct : Accord historique sur le climat.",
            sourceName = "France 24",
            timeAgo = "12 min ago",
            imageUrl = ""
        )
        val spokenFrench = com.example.ui.news.buildSpokenNewsText(storyWithRedundantSummary)
        assertEquals("Accord historique sur le climat", spokenFrench)

        val effectiveLanguages = LocaleHelper.SUPPORTED_LANGUAGES
            .map { LocaleHelper.getEffectiveLanguage(it) }
            .distinct()
        for (lang in effectiveLanguages) {
            val strings = com.example.data.news.NewsRepository.getLocalizedStrings(lang)
            assertTrue("voiceOnLabel missing for $lang", strings.voiceOnLabel.isNotBlank())
            assertTrue("voiceOffLabel missing for $lang", strings.voiceOffLabel.isNotBlank())
            assertTrue("voiceSpeakingLabel missing for $lang", strings.voiceSpeakingLabel.isNotBlank())
        }
    }

    @Test
    fun `news cycle holds before switching screens until all six stories finish speaking when voice is enabled`() {
        assertEquals(6, com.example.ui.home.NEWS_VOICE_CYCLE_STORY_TARGET)

        assertTrue(
            com.example.ui.home.shouldWaitForNewsVoiceCycle(
                isCycleMode = true,
                activeSlide = com.example.ui.home.DashboardScreensaverSlide.NEWS,
                isVoiceEnabled = true,
                cycleStoriesCompleted = false
            )
        )
        assertFalse(
            com.example.ui.home.shouldWaitForNewsVoiceCycle(
                isCycleMode = true,
                activeSlide = com.example.ui.home.DashboardScreensaverSlide.NEWS,
                isVoiceEnabled = true,
                cycleStoriesCompleted = true
            )
        )
        assertFalse(
            com.example.ui.home.shouldWaitForNewsVoiceCycle(
                isCycleMode = true,
                activeSlide = com.example.ui.home.DashboardScreensaverSlide.NEWS,
                isVoiceEnabled = false,
                cycleStoriesCompleted = false
            )
        )
        assertFalse(
            com.example.ui.home.shouldWaitForNewsVoiceCycle(
                isCycleMode = true,
                activeSlide = com.example.ui.home.DashboardScreensaverSlide.WEATHER,
                isVoiceEnabled = true,
                cycleStoriesCompleted = false
            )
        )
    }

    @Test
    fun `dashboard ambient sound engine renders silence when off and soft bounded waveforms for all built-in presets`() {
        val buf = ShortArray(4096)

        val offState = com.example.ui.home.DashboardAmbientSoundEngine.SynthState()
        com.example.ui.home.DashboardAmbientSoundEngine.renderBlock(
            mode = com.example.ui.home.DashboardAmbientSoundPresets.OFF,
            targetAmplitude = 0.25f,
            output = buf,
            state = offState
        )
        assertTrue("OFF mode must render pure silence", buf.all { it.toInt() == 0 })

        for (mode in com.example.ui.home.DashboardAmbientSoundPresets.BUILT_IN_MODES) {
            val state = com.example.ui.home.DashboardAmbientSoundEngine.SynthState()
            com.example.ui.home.DashboardAmbientSoundEngine.renderBlock(
                mode = mode,
                targetAmplitude = 0.25f,
                output = buf,
                state = state
            )
            // Advance a second block past initial fade-in ramp
            com.example.ui.home.DashboardAmbientSoundEngine.renderBlock(
                mode = mode,
                targetAmplitude = 0.25f,
                output = buf,
                state = state
            )
            val peak = buf.maxOf { kotlin.math.abs(it.toInt()) }
            assertTrue("Preset $mode must produce audible PCM samples (peak=$peak)", peak > 50)
            assertTrue("Preset $mode must remain soft without clipping (peak=$peak)", peak < Short.MAX_VALUE)
        }
    }

    @Test
    fun `broadcast text themes provide six distinct motions layouts frames and clean fallback`() {
        val themeIds = listOf(
            com.example.ui.home.BroadcastThemeIds.STUDIO_CLASSIC,
            com.example.ui.home.BroadcastThemeIds.BREAKING_NEWS,
            com.example.ui.home.BroadcastThemeIds.ROYAL_CINEMA,
            com.example.ui.home.BroadcastThemeIds.NEON_CYBER,
            com.example.ui.home.BroadcastThemeIds.EMERALD_SPORT,
            com.example.ui.home.BroadcastThemeIds.MINIMAL_ZEN
        )
        val specs = themeIds.map { com.example.ui.home.resolveBroadcastThemeSpec(it) }
        assertEquals(6, specs.map { it.id }.toSet().size)
        assertEquals(6, specs.map { it.secondaryColor }.toSet().size)
        assertEquals(6, specs.map { it.motionStyle }.toSet().size)
        assertEquals(6, specs.map { it.layoutMode }.toSet().size)
        assertEquals(6, specs.map { it.frameStyle }.toSet().size)
        assertEquals(6, specs.map { it.backdropPalette.baseTop }.toSet().size)
        assertEquals(6, specs.map { it.backdropPalette.shiftTop }.toSet().size)
        assertEquals(6, specs.map { it.backdropPalette.shadowColorContrast }.toSet().size)
        assertTrue(specs.all { it.backdropPalette.shadowColorPrimary != it.backdropPalette.shadowColorContrast })
        assertTrue(specs.first { it.id == com.example.ui.home.BroadcastThemeIds.NEON_CYBER }.showCyberGrid)
        assertFalse(specs.first { it.id == com.example.ui.home.BroadcastThemeIds.STUDIO_CLASSIC }.showCyberGrid)
        assertEquals(
            com.example.ui.home.BroadcastThemeIds.STUDIO_CLASSIC,
            com.example.ui.home.resolveBroadcastThemeSpec("unknown_theme").id
        )
    }

    @Test
    fun `broadcast voice wait and typewriter codepoint helpers behave cleanly`() {
        assertTrue(com.example.ui.home.shouldWaitForBroadcastLineSpeech(isVoiceEnabled = true, speechCompleted = false))
        assertFalse(com.example.ui.home.shouldWaitForBroadcastLineSpeech(isVoiceEnabled = true, speechCompleted = true))
        assertFalse(com.example.ui.home.shouldWaitForBroadcastLineSpeech(isVoiceEnabled = false, speechCompleted = false))

        assertEquals(
            "broadcast_2_${"أهلاً بكم".hashCode()}",
            com.example.ui.home.buildBroadcastSpeechKey(2, "أهلاً بكم")
        )

        val arabicWithEmoji = "مرحبا 🎬 بكم"
        assertEquals("", com.example.ui.home.takeBroadcastCodePoints(arabicWithEmoji, 0f))
        assertEquals(arabicWithEmoji, com.example.ui.home.takeBroadcastCodePoints(arabicWithEmoji, 1f))
        val half = com.example.ui.home.takeBroadcastCodePoints(arabicWithEmoji, 0.6f)
        assertTrue(half.isNotEmpty() && half.length < arabicWithEmoji.length)
        // Ensure no dangling high surrogate
        assertFalse(Character.isHighSurrogate(half.last()))
    }

    @Test
    fun `remote select and menu key helpers recognize tv dpad and menu buttons`() {
        assertTrue(com.example.ui.common.isRemoteSelectKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER))
        assertTrue(com.example.ui.common.isRemoteSelectKey(android.view.KeyEvent.KEYCODE_ENTER))
        assertTrue(com.example.ui.common.isRemoteSelectKey(android.view.KeyEvent.KEYCODE_NUMPAD_ENTER))
        assertTrue(com.example.ui.common.isRemoteSelectKey(android.view.KeyEvent.KEYCODE_BUTTON_A))
        assertFalse(com.example.ui.common.isRemoteSelectKey(android.view.KeyEvent.KEYCODE_DPAD_LEFT))

        assertTrue(com.example.ui.common.isRemoteMenuKey(android.view.KeyEvent.KEYCODE_MENU))
        assertTrue(com.example.ui.common.isRemoteMenuKey(android.view.KeyEvent.KEYCODE_SETTINGS))
        assertFalse(com.example.ui.common.isRemoteMenuKey(android.view.KeyEvent.KEYCODE_DPAD_CENTER))
    }

    @Test
    fun `virtual mouse reaches exact top bottom left and right borders before scrolling`() {
        val width = 1920f
        val height = 1080f
        val step = 50f

        // Moving UP from inside top 120px must still move cursor all the way to 0f (top border)
        val upIntoTopBar = com.example.ui.common.stepVirtualMouse(
            currentX = 960f,
            currentY = 90f,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.UP,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = true
        )
        assertEquals(40f, upIntoTopBar.nextY, 0.01f)
        assertEquals(0, upIntoTopBar.scrollDy)

        val upToExactTop = com.example.ui.common.stepVirtualMouse(
            currentX = 960f,
            currentY = upIntoTopBar.nextY,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.UP,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = true
        )
        assertEquals(0f, upToExactTop.nextY, 0.01f)
        assertEquals(0, upToExactTop.scrollDy)

        // Pressing UP once already at 0f scrolls upward cleanly
        val scrollUpAtTop = com.example.ui.common.stepVirtualMouse(
            currentX = 960f,
            currentY = 0f,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.UP,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = true
        )
        assertEquals(0f, scrollUpAtTop.nextY, 0.01f)
        assertEquals(-220, scrollUpAtTop.scrollDy)
        assertFalse(scrollUpAtTop.reachedEdgeBeyondLimit)

        // Moving DOWN from inside bottom 140px must reach height - 2f (exact bottom edge)
        val downToEdge = com.example.ui.common.stepVirtualMouse(
            currentX = 960f,
            currentY = height - 40f,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.DOWN,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = false
        )
        assertEquals(height - 2f, downToEdge.nextY, 0.01f)
        assertEquals(0, downToEdge.scrollDy)

        // Left & Right borders reach 0f and width - 2f
        val leftToEdge = com.example.ui.common.stepVirtualMouse(
            currentX = 8f,
            currentY = 500f,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.LEFT,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = false
        )
        assertEquals(0f, leftToEdge.nextX, 0.01f)

        val rightToEdge = com.example.ui.common.stepVirtualMouse(
            currentX = width - 8f,
            currentY = 500f,
            viewportWidth = width,
            viewportHeight = height,
            direction = com.example.ui.common.VirtualMouseDirection.RIGHT,
            stepPx = step,
            scrollStepPx = 220,
            canScrollInDirection = false
        )
        assertEquals(width - 2f, rightToEdge.nextX, 0.01f)
    }
}
