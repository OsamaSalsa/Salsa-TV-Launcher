**English** | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.1

![Home Screen](docs/screenshots/home.png)

**A modern, customizable, ultra-fast, and 100% Ad-Free Android TV Launcher & Media Center built with Jetpack Compose.**  
*The All-in-One TV solution that replaces dozens of separate apps on your device.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.1-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
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

### 🆕 What's New in Version 3.1
* **🧸 Kids Mode Profiles & 4 GitHub-Hosted Kids Themes (`Rainbow Unicorn`, `Magic Garden`, `Space Explorer`, `Dino Jungle`)**: Create dedicated **Kids Mode** user profiles protected by a mandatory **4-digit PIN** (entered on profile creation) required to open Settings or edit menus. Kids Mode presents a playful child-friendly layout with a **Settings-only top bar** (Clock, Weather, Date, and Settings avatar button only), **Playpen Sans Arabic** / **Comic Relief** typography, original app icons & Android TV banners preserved as-is, automatic **Family Protection DNS** enabled and locked, and **4 selectable Kids Themes** whose `1920×1080` wallpapers are dynamically fetched and cached from GitHub (`/kids_themes/`).
* **🛒 100% Live `Salsa Store` Database (`https://auratv.freedev.app/index.html`) & Native TV Category Sidebar**: Zero app names or catalog snapshots are bundled or cached inside the APK — every time you open **Salsa Store**, it fetches the latest live app names, icons, descriptions, and download links directly from `https://auratv.freedev.app/index.html` and presents them in the native Android TV categorized interface, starting with **Web Browsers** as the first section, followed by **Social Media**, and then all remaining categories.
* **⌨️ Automatic Soft Keyboard for Web & App Input/Search Fields (Except YouTube)**: Clicking any text box or search input inside web sites, portals, or Mini Downloader pages with the virtual mouse or remote immediately opens a native Android TV text input bar and triggers the default system keyboard (`IME`) — while leaving **YouTube TV** untouched so it uses its own built-in on-screen keyboard.
* **🔐 Per-Profile PIN System, Masked Double-Entry Setup, Profile Entry Lock & Admin `Master PIN` Recovery**: Removed any hardcoded default PIN (`1234` no longer exists). Before locking any app, home section, IPTV category, or family DNS mode, the user must first create a 4-digit PIN by **entering it twice to confirm**. PINs are never displayed in plain text inside Settings or Profile Editor (shown only as masked `••••`), and changing or removing a PIN requires verifying the current PIN first. Each user profile has its own independent PIN and an optional **Lock Profile Entry with PIN** toggle so nobody can switch into that profile without its PIN. The **`Main` profile is the undeletable Admin (`Master`)**: it can edit or reset any other profile's settings/PIN, and its PIN works everywhere as the **`Master PIN Code`** to enter, unlock, or edit any profile if a user forgets their PIN.
* **🎙️ Enhanced Android TV Voice Search Reliability**: Directly binds to Android TV's installed `RecognitionService` (`katniss` / Google TV Speech Services) with clean session teardown, extended Bluetooth remote microphone silence thresholds, and automatic instant-retry fallback so voice search consistently hears your voice on the first press.
* **🏠 Built-in `Launcher Manager` (Local ADB Default HOME Setup — No External PC/Phone Needed)**: Set Salsa TV Launcher as your persistent default HOME launcher directly from inside the app over local ADB (`127.0.0.1:5555`) with persistent RSA key pair authentication, step-by-step wizard (`cmd package set-home-activity` with `pm` fallback), live verification, optional stock launcher disable, one-click stock launcher restoration, and standard HOME chooser fallback — available directly inside **Settings > System & TV Shortcuts > Launcher Manager**.
* **👤 Multi-User Profiles, Per-User Dynamic Theme Accent Color & Isolation (`Profiles / Users`)**: Create multiple independent family profiles — each with its own **Dynamic Theme Accent Color** (8 TV-tuned high-contrast shades that dynamically replace the signature gold/yellow accent across the greeting, Home section icons [Apps, Games, Web, IPTV], Settings, IPTV, and all focus highlights), **Appearance & Wallpaper**, **Screensaver**, **Language**, **IPTV Favorites & Channel Order**, **Apps / Web / Games Order**, optional **4-digit Profile PIN**, and granular **Per-User Section Controls** (Show, PIN-Lock, or Hide **IPTV**, **Apps**, **Web**, or **Games**). Accessible via a sleek flat, borderless circular avatar icon anchored at the outermost top edge (**far-right in LTR, far-left in RTL**).
* **🔢 Quick Numeric Channel Zap (`0–9`) & High-Contrast Channel Badges**: Press any number keys (`0–9`) on your TV remote from the Home Screen, IPTV Channel Browser, or live IPTV Player to jump directly to a channel by number — paired with crisp, high-contrast channel number badges (black digits on a translucent white badge).
* **🎞️ Local Device / USB Video Screensaver (`Media3 / ExoPlayer`)**: In addition to clean fullscreen `m.youtube.com` streams with zero overlays, pick any local video file directly from your TV storage or USB drive via Android's zero-permission Storage Access Framework (SAF) for seamless looping fullscreen playback.
* **📡 Broadcast Text Showcase (12 Animated Themes, Dynamic Dark Backdrops & Voice Reader)**: 12 distinct studio presentation themes — 6 classics plus 6 creative animated themes (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — each with unique motion, layouts, and per-theme dark dynamic backdrops with shifting contrasting colored shadows + live preview thumbnails in Settings, editable custom lines, adjustable duration (`3s–15s`), **Voice Reader (TTS)** that waits for narration to finish before advancing, and ambient/MP3 music.
* **📺 IPTV Smart Error Recovery (`Next Channel` + 15s Auto-Retry) & Secret 3× OK Gift Unlock**: When an IPTV channel fails to load, displays a focused single-action overlay with **Next Channel** only and a live **15-second countdown** that automatically retries the same channel if unselected. Plus, pressing `OK` 3 times on the empty-state IPTV card unlocks built-in curated IPTV playlists with a celebratory gift box animation.
* **🛒 Salsa Store Top Categories & Clean `m.youtube.com` Video Screensaver**: **Salsa Store** prioritizes **Web Browsers** and **Social Media** at the top of its 9-category catalog of legitimate Android TV utilities and players (with offline fallback); and the Cinematic Video Screensaver always streams cleanly in fullscreen via `m.youtube.com` with zero on-screen text, HUD, or player overlays.
* **🎨 Live UI Settings HUD — Direct Clock Format Toggle**: Instant live-preview bar right on the Home Screen now features a dedicated **Clock Format** toggle (**`24h • 22:00`** vs **`12h • AM/PM`**) directly alongside Clock Size, Date Format, 4K wallpapers, brightness, app vertical offset, grid density, glass opacity, custom greetings, top bar icons, weather toggle, Night Eye-Comfort filter, Material You dynamic colors, and Low-RAM mode.
* **🖥️ Smart Dashboard Screensaver, News Voice Reader & Unified High-Clarity Music**: Display **Weather**, **News**, **Economy**, **Football**, or **Auto-Cycle All** (**News 60s → Weather 10s → Economy 10s → Football 10s → returns to News advancing automatically to the next section**). News screensaver supports **Voice Reader (TTS)** that finishes reading all 6 items before switching, and all Info Dashboards share the unified crystal-clear background music tracks (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambient soundscapes, and custom MP3) with automatic ducking when TTS speaks.
* **🌙 10-Minute Default Screensaver Timeout**: Default screensaver inactivity timer updated to **10 minutes** (configurable: 2, 5, 10, 15, 30 min, or Disabled for system handover).
* **🖱️ Edge-to-Edge Virtual Mouse, Context-Aware 3-Lines Menu (`☰`) & Startup Splash Version**: Virtual mouse cursor in web portals & HTML5 games cleanly reaches the very top and bottom edges; Three-Lines **Menu (`☰`)** opens **Settings** on Home, **IPTV Settings** in IPTV, and toggles **Virtual Mouse** in Web Portals/Games; reliable remote **Long Press `OK`** menu without accidental launch on release; compact **Salsa Store** icon; `v3.0` splash indicator; and seamless **GitHub OTA auto-updates** with SHA-256 checksum & APK signing certificate verification.
* **🌐 Live 4-Dashboard Hub, Web Portals & 21-Language v3.0 Engine**: Direct top-bar access to fullscreen **Weather**, **News** (21 languages, 5 deduplicated sections), **Economy & Markets**, and **Football (365Scores)** dashboards, default web shortcuts (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), non-clipping top bar, and a lightweight 60fps v3.0 engine.

### ⚡ Extreme Performance & Instant App Launch
* **Parallel Icon & Banner Preloading**: App packages, icons, and banners are decoded concurrently across multi-core CPUs in the background, appearing instantly on boot without freezing.
* **Instant 110ms D-Pad Focus Navigation**: Redesigned hardware-accelerated `graphicsLayer` rendering and snappy 110ms transitions eliminate all cursor lag when navigating between apps.
* **GPU-Optimized Dynamic Ambient Glow**: Replaced heavy runtime blur and noise layers with fluid hardware-rendered radial gradients for silky-smooth 60fps scrolling on any TV hardware.
* **Clean Home Screen (Bottom Edit Button Removed)**: Streamlined bottom area so user focus stays entirely on apps and channels, while keeping quick reordering via remote **Long Press `OK`**.

### 🌙 Intelligent Screensaver & External Screensaver Handover
* **Smart External Handover (`Disabled` Mode)**: Disabling the in-app screensaver automatically clears window wake locks (`FLAG_KEEP_SCREEN_ON`), allowing your Android TV's system screensaver or external ambient app to trigger freely.
* **Built-in Ambient Sleep Mode (`Enabled`)**: Enabled by default after **10 minutes** of inactivity (configurable: 2, 5, 10, 15, or 30 mins), keeping the TV awake to trigger your chosen built-in screensaver:
  * **Clock & Date Mode**: Luminous time and date with drift protection.
  * **4K Wallpaper & Info Mode**: 45-degree corner gradient with clock, date, and live greeting.
  * **Cinematic Video Screensaver & Custom Streams (`m.youtube.com`)**: Fullscreen relaxing fireplace, winter cabin, or tropical waterfall streams playing cleanly via `m.youtube.com` with zero on-screen text, HUD, or player overlays, plus the ability to **add custom YouTube video IDs/URLs** or pick a local device video file, delete or restore presets, and trigger an instant **Live Screensaver Preview** from Settings.
  * **Photo Slideshow Screensaver (`Slideshow`)**: Turn your TV into a living gallery using a custom folder from device storage/USB, selected photos, or curated 4K collections:
    * **Slide Duration & Shuffle**: Configurable interval per photo (`5s`, `10s`, `15s`, `30s`, `1m`, `5m`) with optional shuffle order.
    * **Photo Motion**: Choose **Ken Burns (Pan & Zoom)**, **Gentle Zoom**, or **Static**.
    * **Entry Transitions**: **Crossfade**, **Slide Horizontal**, **Zoom & Fade**, **Soft Scale Reveal**, **3D Perspective Flip**, or **Random**.
    * **Atmospheric Particle Overlays**: **Falling Snow ❄️**, **Glowing Fireflies ✨**, **Gentle Rain 🌧️**, **Autumn Leaves 🍂**, **Sakura Petals 🌸**, **Twinkling Stars ⭐**, or **Bokeh Lights 🔮**.
    * **Corner Clock & Ambient Music**: Displays the 45° corner clock, date, and greeting (matching the static wallpaper screensaver) plus crystal-clear built-in music tracks (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambient soundscapes) or custom MP3 audio with independent volume & night dimming controls.

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

### 🎙️ Universal Voice Search & Interactive IPTV
* **Universal Voice & Instant Search (`🎤`)**: Search across all installed TV apps, live IPTV channels, and web/YouTube queries simultaneously using your TV remote microphone or on-screen keyboard.
* **Quick Control Center (`Control Center`)**: Glassmorphic quick-access panel to toggle 12h/24h clock format, toggle dynamic background, start the Ambient Screensaver immediately, launch Voice Search, or open Wi-Fi & full Settings (with the option to skip Control Center for Direct Settings or show both icons).
* **Multi-Playlist IPTV Manager, Interactive Info Bar & Smart Error Recovery**: Add, switch between, refresh, or delete **multiple M3U playlists** and custom XMLTV EPG URLs. While watching live IPTV, pressing `OK` on the remote opens the Cinema Info Bar with an embedded horizontal **Channel Carousel Strip** (`◀ / ▶`), live EPG, Sleep Timer, Aspect Ratio (`Fit`/`Fill`/`Zoom`), Category PIN Lock, Auto-Resume Last Channel, and smart error recovery (**Next Channel** button with **15-second automatic retry**).
* **Clean Startup Showcase & Smart 240dp Clock Header**: Launches with apps and channels positioned at the bottom (6 named levels from *High* to *Ultra Low*, default `450`), an unobtrusive parked cursor (no button highlighted until you press a key), and a full-size clock that automatically shrinks into a compact header as soon as rows scroll within `240dp` of the top.
* **Smart Web Bookmarks (High-Res Icons & YouTube TV Mode)**: Custom website shortcuts automatically extract and cache high-resolution Touch/Favicon artwork, and any YouTube bookmark opens directly in the full **YouTube TV Leanback** remote-friendly interface.
* **Recent Apps Row & GitHub OTA Updater**: Optional horizontal Recent Apps bar and 1-click GitHub OTA update checking with SHA-256 hash and APK signature verification.

### 🔒 Privacy, App Storage Manager, Family Protection (PIN-Gated) & Organization
* **Built-in App Storage Manager**: Inspect all installed user apps sorted by APK size (`MB`), open system App Info, or **uninstall apps directly** from Settings to free up TV storage.
* **10 Quick System Shortcuts Grid (with Built-in `Launcher Manager`)**: One-click access to **Launcher Manager (Local ADB Default HOME Setup)**, **Developer Options**, **Wi-Fi**, **Ethernet**, **Display**, **Sound**, **Date & Time**, **All System Settings**, **App Info**, and **App Storage Manager**.
* **Dual-Mode DNS Shield (AdBlock & PIN-Protected Family Mode)**: Choose between **Ads & Trackers Blocking** or **Family Protection Mode** (blocks ads, trackers, and adult websites system-wide). **Exiting Family Protection Mode or changing the PIN while Family Mode is active strictly requires entering the 4-digit PIN code.**
* **PIN-Protected App & IPTV Category Lock**: Lock sensitive apps and IPTV categories with a 4-digit PIN code.
* **App Reordering & Hiding**: Move, categorize (Apps vs. Games), or hide pre-installed system apps from the launcher grid.
* **Safe Remote Navigation**: Prevents accidental exits to system desktop when pressing the Back key on the Home screen.

> ⚠️ **Important Note on Kids / Family Protection Mode**: Exiting Family Protection Mode inside the launcher is protected by your 4-digit PIN. However, please note that **a launcher alone cannot prevent a child from exiting via another installed app or the system `Home` button** unless **Salsa TV Launcher** is set as the device's **Default Home App** and you also lock **System Settings** (or other launchers/browsers) with the App Lock PIN.

---

## 🛡️ Security & Permissions Transparency

Salsa TV Launcher is built with a strict privacy-first, zero-telemetry architecture:
* **HTTPS-By-Default (`network_security_config.xml`)**: Cleartext HTTP traffic is disabled globally (`cleartextTrafficPermitted="false"`), with scoped exceptions strictly for legacy services that do not support HTTPS.
* **WebView Safe Browsing Enabled**: Google Safe Browsing is enabled across all embedded WebViews (`EnableSafeBrowsing=true`) with metrics opt-out (`MetricsOptOut=true`).
* **Verified OTA Auto-Updates**: Before launching any downloaded update APK from GitHub, `AppUpdateManager` verifies the **SHA-256 hash** (from `version.json`), validates the **package name**, and verifies that the **APK signing certificate** matches the currently installed application signature.
* **Zero Broad Storage Permissions (`No MANAGE_EXTERNAL_STORAGE`)**: All user-selected wallpapers, local videos, MP3 files, photo folders, and JSON backups use Android's zero-permission **Storage Access Framework (SAF)** document pickers.

| Permission | Why It Is Needed |
| :--- | :--- |
| `INTERNET` & `ACCESS_NETWORK_STATE` | Required for streaming IPTV channels, fetching live Weather/News/Economy/Football dashboards, Salsa Store catalog, and GitHub OTA updates. |
| `ACCESS_COARSE_LOCATION` | Optional approximate (city-level) location fallback for the local Weather widget & dashboard (no fine GPS tracking). |
| `RECORD_AUDIO` | Optional; used exclusively when you trigger the in-app **Voice Search (`🎤`)** dialog to search apps, IPTV channels, or the web by voice. |
| `REQUEST_INSTALL_PACKAGES` | Used only when you explicitly download and install an APK via **Mini Downloader**, **Salsa Store**, or the **GitHub OTA Auto-Updater**. |
| `REQUEST_DELETE_PACKAGES` | Used only when you explicitly choose **Uninstall** on an app inside the **App Storage Manager** or App Management menu. |
| `FOREGROUND_SERVICE` & `BIND_VPN_SERVICE` | Used only when you enable the local **AdGuard DNS / Family Protection Shield** to route DNS queries to AdGuard DNS without external servers. |
| `RECEIVE_BOOT_COMPLETED` | Allows the launcher and optional DNS Shield to start cleanly when your Android TV powers on. |

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

> ℹ️ **Platform Note**: Salsa TV Launcher is designed primarily for **Android TV, Google TV, and TV Boxes** controlled via a remote control (`D-Pad`). *(Support for small mobile/touch screens is currently **experimental / under active development**).*

### Option 1: Direct APK Download
Download the latest compiled release APK directly to your Android TV device:

👉 **[Download Latest Salsa TV Launcher APK](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Downloader Short Code / Direct Link: aftv.news/5157967](http://aftv.news/5157967)**

### Option 2: Installation via ADB
```bash
# Connect to your Android TV IP address
adb connect <YOUR_TV_IP_ADDRESS>:5555

# Install the APK
adb install -r salsa-tv-launcher.apk
```

### Option 3: Setting as Default Home Launcher (Built-in `Launcher Manager` via Local ADB)
1. Open **Settings > System & TV Shortcuts** inside Salsa TV Launcher and select **Launcher Manager**.
2. Follow the 3-step wizard: enable **Developer Options & USB/Network Debugging**, press **Connect** (approve *"Always allow"* once on your TV), and press **Set as HOME** to make Salsa TV Launcher your persistent default HOME launcher without needing an external PC or phone (with one-click **Restore stock launcher** anytime, plus standard Android Home chooser fallback).

---

## 🎮 Remote Control Shortcuts

| Remote Key | Action |
| :--- | :--- |
| **D-Pad Arrows** | Smooth high-speed navigation across apps, channels, and settings. |
| **OK / D-Pad Center** | Launch selected app or play IPTV channel. |
| **Long Press OK** | Opens the App/Channel Management Menu (Reorder, Lock with PIN, Hide, Game category, App Info/Uninstall). |
| **Number Keys (`0–9`)** | **Quick Channel Zap**: type a channel number on the Home Screen, IPTV Channel Browser, or live IPTV Player to jump directly to that channel (`OK` to confirm, `Back` to edit). |
| **Menu (`☰` Three Lines)** | Context-aware: opens **Settings** on the Home Screen, opens **IPTV Settings** inside IPTV (channel browser & player, while `INFO`/`GUIDE` toggle the player OSD), and toggles **Virtual Mouse Mode** in Web Portals & Games. |
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
This repository hosts the official compiled release APK, multilingual documentation, screenshots, and verified OTA auto-update manifests (`version.json`).

<div align="center">
  <sub>Made with ❤️ for a clean, fast, and ad-free Android TV experience worldwide.</sub>
</div>
