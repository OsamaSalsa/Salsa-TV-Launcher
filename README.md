**English** | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.0

![Home Screen](docs/screenshots/home.png)

**A modern, customizable, ultra-fast, and 100% Ad-Free Android TV Launcher & Media Center built with Jetpack Compose.**  
*The All-in-One TV solution that replaces dozens of separate apps on your device.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 The All-in-One Solution

Most smart TV launchers today are cluttered with forced ads, heavy video previews, unwanted recommendations, and laggy user interfaces. 

**Salsa TV Launcher** is built to be the exact opposite. It is not just a home screen; it is an all-in-one media center that replaces the need for separate TV apps into one clean, fast, and completely ad-free hub.

### ✨ Core Integrated Modules
1. **🚀 Ultra-Fast & Lightweight Launcher**: Sub-second app startup, GPU-accelerated focus movement, parallel icon preloading, and 0% CPU bloat.
2. **📺 Built-in IPTV & Favorites Row**: M3U playlist player with instant 1-click playback for favorite channels directly on the home screen.
3. **🎥 MiniTube**: Lightweight YouTube TV Leanback experience with fast guest playback and full TV remote controls.
4. **📥 Mini Downloader**: High-speed file and APK downloader with smart 1-click package installer.
5. **🛒 Salsa Store**: Clean web app marketplace to browse and install Android TV tools and utilities.
6. **🕹️ 4 Gaming Portals**: Thousands of instant online HTML5 games without downloads (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ System-Wide Ad & Tracker Blocker**: Built-in AdGuard DNS protection shields your entire TV from intrusive popups, banners, and telemetry.

---

## 🌟 New Features & Latest Enhancements

### 🆕 What's New in Version 3.0
* **📡 Broadcast Text Showcase (6 Animated Themes, Dynamic Dark Backdrops & Voice Reader)**: 6 distinct studio presentation themes with unique motion, layouts, surrounding accents, and per-theme dark dynamic backdrops with shifting contrasting colored shadows + live preview thumbnails in Settings, editable custom lines, adjustable duration (`3s–15s`), **Voice Reader (TTS)** that waits for narration to finish before advancing, and optional soft ambient audio or local MP3 playback.
* **📺 IPTV Smart Error Recovery (`Next Channel` + 15s Auto-Retry)**: When an IPTV channel fails to load, displays a focused single-action overlay with **Next Channel** only and a live **15-second countdown** that automatically retries the same channel if unselected.
* **🎨 Live UI Settings HUD — Direct Clock Format Toggle**: Instant live-preview bar right on the Home Screen now features a dedicated **Clock Format** toggle (**`24h • 22:00`** vs **`12h • AM/PM`**) directly alongside Clock Size, Date Format, 4K wallpapers, brightness, app vertical offset, grid density, glass opacity, custom greetings, top bar icons, weather toggle, Night Eye-Comfort filter, Material You dynamic colors, and Low-RAM mode.
* **🖥️ Smart Dashboard Screensaver & News Voice Reader**: Display **Weather**, **News**, **Economy**, **Football**, or **Auto-Cycle All** (**News 60s → Weather 10s → Economy 10s → Football 10s → returns to News advancing automatically to the next section**). News screensaver supports **Voice Reader (TTS)** that finishes reading all 6 items before switching, plus selectable soft ambient soundtracks.
* **🌙 10-Minute Default Screensaver Timeout**: Default screensaver inactivity timer updated to **10 minutes** (configurable: 2, 5, 10, 15, 30 min, or Disabled for system handover).
* **🖱️ Edge-to-Edge Virtual Mouse, TV Remote Fixes & Startup Splash Version**: Virtual mouse cursor in web portals & HTML5 games cleanly reaches the very top and bottom edges; reliable TV remote long-press menu without accidental launch on release; refreshed compact **Salsa Store** icon; subtle `v3.0` version indicator at the bottom of the Salsa TV startup splash screen; and seamless **GitHub OTA auto-updates** for users on older versions.
* **🌐 Live 4-Dashboard Hub, Web Portals & 21-Language v3.0 Engine**: Direct top-bar access to fullscreen **Weather**, **News** (21 languages, 5 deduplicated sections), **Economy & Markets**, and **Football (365Scores)** dashboards, default web shortcuts (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), non-clipping top bar, and a lightweight 60fps v3.0 engine.

### ⚡ Extreme Performance & Instant App Launch
* **Parallel Icon & Banner Preloading**: App packages, icons, and banners are decoded concurrently across multi-core CPUs in the background, appearing instantly on boot without freezing.
* **Instant 110ms D-Pad Focus Navigation**: Redesigned hardware-accelerated `graphicsLayer` rendering and snappy 110ms transitions eliminate all cursor lag when navigating between apps.
* **GPU-Optimized Dynamic Ambient Glow**: Replaced heavy runtime blur and noise layers with fluid hardware-rendered radial gradients for silky-smooth 60fps scrolling on any TV hardware.
* **Clean Home Screen (Bottom Edit Button Removed)**: Streamlined bottom area so user focus stays entirely on apps and channels, while keeping quick reordering via remote long-press (`OK` or `Menu`).

### 🌙 Intelligent Screensaver & External Screensaver Handover
* **Smart External Handover (`Disabled` Mode)**: Disabling the in-app screensaver automatically clears window wake locks (`FLAG_KEEP_SCREEN_ON`), allowing your Android TV's system screensaver or external ambient app to trigger freely.
* **Built-in Ambient Sleep Mode (`Enabled`)**: Enabled by default after **10 minutes** of inactivity (configurable: 2, 5, 10, 15, or 30 mins), keeping the TV awake to trigger your chosen built-in screensaver:
  * **Clock & Date Mode**: Luminous time and date with drift protection.
  * **4K Wallpaper & Info Mode**: 45-degree corner gradient with clock, date, and live greeting.
  * **Cinematic Video Screensaver & Custom Streams**: Fullscreen relaxing fireplace, winter cabin, or tropical waterfall streams without intrusive banners or sign-in prompts, plus the ability to **add custom YouTube video IDs/URLs**, delete or restore presets, and trigger an instant **Live Screensaver Preview** from Settings.
  * **Photo Slideshow Screensaver (`Slideshow`)**: Turn your TV into a living gallery using a custom folder from device storage/USB, selected photos, or curated 4K collections:
    * **Slide Duration & Shuffle**: Configurable interval per photo (`5s`, `10s`, `15s`, `30s`, `1m`, `5m`) with optional shuffle order.
    * **Photo Motion**: Choose **Ken Burns (Pan & Zoom)**, **Gentle Zoom**, or **Static**.
    * **Entry Transitions**: **Crossfade**, **Slide Horizontal**, **Zoom & Fade**, **Soft Scale Reveal**, **3D Perspective Flip**, or **Random**.
    * **Atmospheric Particle Overlays**: **Falling Snow ❄️**, **Glowing Fireflies ✨**, **Gentle Rain 🌧️**, **Autumn Leaves 🍂**, **Sakura Petals 🌸**, **Twinkling Stars ⭐**, or **Bokeh Lights 🔮**.
    * **Corner Clock & Ambient Music**: Displays the 45° corner clock, date, and greeting (matching the static wallpaper screensaver) plus built-in relaxing ambient soundscapes or custom MP3 audio with independent volume & night dimming controls.

### 🕒 Comprehensive Clock, Date & Greeting Customization
* **Multilingual Typography (21 Languages)**: Complete localization in **Arabic (Cairo Font)**, **English**, **German**, **Spanish**, **French**, **Italian**, **Russian**, **Portuguese**, **Turkish**, **Polish**, **Dutch**, **Greek**, **Indonesian**, **Vietnamese**, **Thai**, **Chinese**, **Japanese**, **Korean**, **Hindi**, **Urdu (RTL)**, and **Persian (RTL)**.
* **Clock Customization**: 12-hour / 24-hour toggle, optional live seconds display, and 3 sizing choices (Compact, Medium, Large).
* **American & International Date Formats**: Choose US Numeric (`MM/dd/yyyy`), US Long (`MMMM d, yyyy`), US Full with Day (`EEEE, MMMM d, yyyy`), US Compact (`EEE, MMM d`), International (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), European Dot (`dd.MM.yyyy`), Full International (`EEEE, d MMMM yyyy`), or Hidden.
* **Greeting Styles**: Poetic hourly greetings, Warm greetings, Brief greeting, Custom personal greeting, or Hidden.

### 🎛️ Launcher Customization & Home Screen Layout
* **Top Bar Control Center & Direct Settings Mode**: Choose whether the top bar shows the **Quick Control Center** icon, skips the Control Center to show the **Direct Settings** gear icon, or **Shows Both Icons Together** on the Home Screen.
* **Default Focus on Settings**: Launcher boots with the cursor focused directly on the top-right Settings / Control Center icon for rapid TV configuration.
* **Vertical Apps Top Offset**: Shift app rows downward (6 named levels from High to Ultra Low, default 450) to showcase high-resolution 4K wallpapers and dynamic background aurora effects.
* **Custom Grid Density**: Choose between 4, 5, 6, or 7 apps per row to match your display size and viewing distance.
* **Glassmorphic Opacity & Material You Dynamic Colors**: Adjust frosted glass card translucency from 10% to 100%, and optionally enable **Material You Dynamic Colors** to adapt accent tones to your system palette.
* **Sideloaded / Non-TV Apps Toggle**: Switch between showing **TV-Optimized Apps Only** or **All Installed Android Apps** (including sideloaded mobile APKs), with 1-click options to restore all hidden apps or reset app order.

### 🎙️ Universal Voice Search, System Voice Assistant & Interactive IPTV
* **Universal Voice & Instant Search (`🎤`)**: Search across all installed TV apps, live IPTV channels, and web/YouTube queries simultaneously using your TV remote microphone or on-screen keyboard.
* **System-Level Default Digital Voice Assistant**: Built-in `SalsaVoiceInteractionService` and `SalsaRecognitionService` allow setting **Salsa TV** as your Android TV's default system voice assistant so the remote's microphone button works natively.
* **Quick Control Center (`Control Center`)**: Glassmorphic quick-access panel to toggle 12h/24h clock format, toggle dynamic background, start the Ambient Screensaver immediately, launch Voice Search, or open Wi-Fi & full Settings (with the option to skip Control Center for Direct Settings or show both icons).
* **Multi-Playlist IPTV Manager, Interactive Info Bar & Smart Error Recovery**: Add, switch between, refresh, or delete **multiple M3U playlists** and custom XMLTV EPG URLs. While watching live IPTV, touching the screen or pressing `OK` on the remote opens the Cinema Info Bar with an embedded horizontal **Channel Carousel Strip** (`◀ / ▶`), live EPG, Sleep Timer, Aspect Ratio (`Fit`/`Fill`/`Zoom`), Category PIN Lock, Auto-Resume Last Channel, and smart error recovery (**Next Channel** button with **15-second automatic retry**).
* **Clean Startup Showcase & Smart 240dp Clock Header**: Launches with apps and channels positioned at the bottom (6 named levels from *High* to *Ultra Low*, default `450`), an unobtrusive parked cursor (no button highlighted until you press a key), and a full-size clock that automatically shrinks into a compact header as soon as rows scroll within `240dp` of the top.
* **Smart Web Bookmarks (High-Res Icons & YouTube TV Mode)**: Custom website shortcuts automatically extract and cache high-resolution Touch/Favicon artwork, and any YouTube bookmark opens directly in the full **YouTube TV Leanback** remote-friendly interface.
* **Recent Apps Row & GitHub OTA Updater**: Optional horizontal Recent Apps bar and 1-click GitHub OTA update checking with automatic APK verification.

### 🔒 Privacy, App Storage Manager, Family DNS & Organization
* **Built-in App Storage Manager**: Inspect all installed user apps sorted by APK size (`MB`), open system App Info, or **uninstall apps directly** from Settings to free up TV storage.
* **8 Quick System Shortcuts Grid**: One-click access to **Wi-Fi**, **Ethernet**, **Display**, **Sound**, **Date & Time**, **All System Settings**, **App Info**, and **App Storage Manager**.
* **Dual-Mode DNS Shield (AdBlock & Family Protection)**: Choose between **Ads & Trackers Blocking** or **Family Protection Mode** (blocks ads, trackers, and adult websites system-wide).
* **PIN-Protected App Lock**: Lock sensitive apps and IPTV categories with a 4-digit PIN code.
* **App Reordering & Hiding**: Move, categorize (Apps vs. Games), or hide pre-installed system apps from the launcher grid.
* **Safe Remote Navigation**: Prevents accidental exits to system desktop when pressing the Back key on the Home screen.

---

## 📱 Screenshots

<div align="center">

| Home Screen & Ambient Glow | 4K Wallpaper & Custom Photos |
| :---: | :---: |
| *![Home Screen](docs/screenshots/home_screen.png)* | *![Wallpapers](docs/screenshots/wallpaper_grid.png)* |

| Integrated IPTV Player | Salsa Store |
| :---: | :---: |
| *![IPTV Player](docs/screenshots/iptv_player.png)* | *![Salsa Store](docs/screenshots/aura_store.png)* |

| Gaming Portals | Mini Downloader |
| :---: | :---: |
| *![Gaming Portals](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK Download & Installation

### Option 1: Direct APK Download
Download the latest compiled release APK directly to your TV or phone:

👉 **[Download Latest Salsa TV Launcher APK](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Downloader Short Code / Direct Link: aftv.news/5157967](http://aftv.news/5157967)**

### Option 2: Installation via ADB
```bash
# Connect to your Android TV IP address
adb connect <YOUR_TV_IP_ADDRESS>:5555

# Install the APK
adb install -r salsa-tv-launcher.apk
```

### Option 3: Setting as Default Home Launcher
1. Open **Settings** inside Salsa TV Launcher.
2. Select **Set as Default Launcher**.
3. Choose **Salsa TV Launcher** and click **Always**.

---

## 🎮 Remote Control Shortcuts

| Remote Key | Action |
| :--- | :--- |
| **D-Pad Arrows** | Smooth high-speed navigation across apps, channels, and settings. |
| **OK / D-Pad Center** | Launch selected app or play IPTV channel. |
| **Long Press OK / Menu (`☰` Three Lines)** | Open App/Channel Management Menu when focused on an item (Reorder, Lock with PIN, Hide, Game category), or jump focus directly to apps/channels when pressed on the top bar. |
| **Voice / Google Assistant Button (`🎤`)** | Opens in-app Voice Search to find apps, IPTV channels, or launch web searches. |
| **Settings Button (`KEYCODE_SETTINGS`)** | Opens the Quick Control Center or jumps directly to Settings (based on your Top Bar Icon preference). |
| **Back Button** | Returns to previous screen or safely returns to Home without exiting to OS. |

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **Architecture**: MVVM + Coroutines & StateFlow
* **Image Engine**: [Coil](https://coil-kt.github.io/coil/)
* **Media Playback**: [Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 License & Copyright

Designed and developed by **© Osama Salsa**.  
All rights reserved.

<div align="center">
  <sub>Made with ❤️ for a clean, fast, and ad-free Android TV experience worldwide.</sub>
</div>
