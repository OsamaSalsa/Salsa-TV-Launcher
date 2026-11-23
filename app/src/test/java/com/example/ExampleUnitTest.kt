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
    fun `app build version is 2_1 with versionCode 7 and supports auto update checks`() {
        assertEquals("2.1", BuildConfig.VERSION_NAME)
        assertEquals(7, BuildConfig.VERSION_CODE)
        assertEquals("com.salsa.tvlauncher", BuildConfig.APPLICATION_ID)
        // Older v2.0 (code 6) upgrades to v2.1 (code 7)
        assertTrue(AppUpdateManager.isRemoteNewer(7, "2.1", 6, "2.0"))
        // Current v2.1 (code 7) upgrades to future v2.2 (code 8)
        assertTrue(AppUpdateManager.isRemoteNewer(8, "2.2", 7, "2.1"))
        // Same version does not trigger update
        assertFalse(AppUpdateManager.isRemoteNewer(7, "2.1", 7, "2.1"))
        // Lower versionCode never triggers downgrade
        assertFalse(AppUpdateManager.isRemoteNewer(6, "2.0", 7, "2.1"))
        assertFalse(AppUpdateManager.isRemoteNewer(6, "9.9", 7, "2.1"))
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
}
