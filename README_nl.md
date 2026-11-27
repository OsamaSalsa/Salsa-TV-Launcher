[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | **Nederlands** | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 🌌 Salsa TV Launcher

**Ervaar de toekomst van home-entertainment met een moderne, razendsnelle en filmische tv-interface.**

[![Android](https://img.shields.io/badge/Platform-Android%20TV-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Download APK](https://img.shields.io/badge/Download-APK%20Release-FF0055?style=for-the-badge&logo=github)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

### 📥 [Klik hier om de nieuwste APK-versie te downloaden (Klaar voor installatie)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

<img src="docs/screenshots/home.png" alt="Salsa TV Launcher Main Preview" width="800"/>

</div>

---

## ✨ Overzicht

**Salsa TV Launcher** is niet zomaar een startscherm; het is een compleet entertainment-ecosysteem dat vanaf de basis is opgebouwd voor **Android TV en Smart TV Boxen**. Volledig gebouwd met **Kotlin** en **Jetpack Compose**, combineert Salsa een vloeiende, filmische "Glassmorphism"-esthetiek met krachtige ingebouwde tools zoals een native **IPTV / Live TV-speler**, een **Cloud Gaming Hub**, de **Salsa App Store** en een **Mini Downloader** voor het direct installeren van APK-pakketten.

Speciaal ontworpen voor tv-afstandsbedieningen (D-Pad-navigatie), waarbij elke animatie, focusstatus en overgang is afgestemd op een soepele 60 FPS-ervaring zonder haperingen.

---

## 🚀 Belangrijkste Functies

### 🆕 Nieuw in Versie 3.0
* **🏠 Ingebouwde `Launcher Manager` (Standaard HOME via lokale ADB — zonder externe pc of telefoon)**: Stel Salsa TV Launcher rechtstreeks vanuit de app in als uw permanente standaard HOME-launcher via lokale ADB (`127.0.0.1:5555`) met opgeslagen RSA-sleutelpaar, 3-stappenwizard (`cmd package set-home-activity` met `pm`-fallback), directe verificatie, herstel van de standaard-launcher met 1 klik en het Android-systeemkeuzemenu als alternatief (**Instellingen > Systeem & TV-snelkoppelingen > Launcher Manager**).
* **👤 Meerdere Gebruikersprofielen, Dynamische Profiel-Accentkleur & Isolatie per Gebruiker (`Profiles / Users`)**: Maak meerdere onafhankelijke gezinsprofielen — elk met een eigen **Dynamische Thema-Accentkleur** (8 contrastrijke tinten afgestemd op tv die dynamisch het geel/goud vervangen in de begroeting, startschermpictogrammen [Apps, Games, Web, IPTV], Instellingen, IPTV en alle focusranden), eigen **Uiterlijk & Achtergrond**, **Screensaver**, **Taal**, **IPTV-favorieten & Zendervolgorde**, **Volgorde van Apps / Web / Games**, optionele **4-cijferige Profiel-PIN** en **sectiebeheer per gebruiker** (**IPTV**, **Apps**, **Web** of **Games** tonen, vergrendelen met PIN of verbergen).
* **🔢 Snel Zappen met Cijfertoetsen (`0–9`) & Hoog-Contrast Zenderbadges**: Druk op cijfertoetsen (`0–9`) op uw afstandsbediening vanuit het Startscherm, de IPTV-zenderlijst of de live speler om direct naar een zendernummer te springen — voorzien van duidelijke hoog-contrast zendernummerbadges (zwarte cijfers op een halftransparante witte achtergrond).
* **🎞️ Lokale Video-screensaver vanaf Apparaat / USB (`Media3 / ExoPlayer`)**: Kies naast schone `m.youtube.com`-streams op volledig scherm zonder overlays elk lokaal videobestand vanaf uw TV-opslag of USB-stick via het veilige **Storage Access Framework (SAF)** zonder opslagrechten.
* **📡 Broadcast Tekst-screensaver (12 Geanimeerde Thema's, Donkere Dynamische Achtergronden & Voorleesfunctie)**: 12 unieke studiothema's — 6 klassiekers plus 6 creatieve nieuwe thema's (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — met eigen animaties, lay-outs en **per thema een donkere dynamische achtergrond met wisselende gekleurde schaduwen** + live voorbeelden in Instellingen, bewerkbare regels (`3–15s`), **Spraaklezer (TTS)** die wacht tot het voorlezen klaar is en optionele achtergrondmuziek of lokale MP3.
* **📺 IPTV Slim Foutbeheer (`Volgende Zender` + 15s) & Geheim 3× OK Cadeau**: Wanneer een IPTV-zender niet laadt, verschijnt een overzichtelijke melding met één gefocuste knop (**Volgende Zender**) en een **afteltimer van 15 seconden** die automatisch opnieuw probeert. Daarnaast ontgrendelt 3× drukken op `OK` op de lege IPTV-kaart ingebouwde afspeellijsten met een feestelijke cadeau-animatie.
* **🛒 Salsa Store Topcategorieën & Schone `m.youtube.com` Video-screensaver**: **Salsa Store** plaatst **Webbrowsers** en **Sociale Media** bovenaan de catalogus van 9 categorieën met samengestelde Android TV-hulpprogramma's en spelers (met offline fallback); de video-screensaver speelt altijd schoon schermvullend via `m.youtube.com` zonder tekst of overlays op het beeld.
* **🎨 Live UI-instellingen (`Live UI Settings`) — Directe Kloknotatie**: Het zwevende menu op het startscherm bevat nu een directe keuze voor **Kloknotatie** (**`24u • 22:00`** / **`12u • AM/PM`**) naast klokgrootte en datumnotatie, plus live voorbeeld voor 4K-achtergronden, helderheid, rijhoogte, raster, transparantie, begroeting, weer, nachtfilter, Material You en Lite-modus.
* **🖥️ Dashboard-screensavers, Nieuws-voorleesfunctie & Geünificeerde Heldere Muziek**: Toon **Weer**, **Nieuws**, **Economie**, **Voetbal** of **Alles automatisch wisselen**; de Nieuws-screensaver biedt **Spraaklezer (TTS)** voor alle 6 berichten vóór het wisselen, en alle dashboards delen dezelfde kristalheldere muzieknummers (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambient & MP3) met automatische volumeverlaging bij spraak.
* **🌙 Standaard Screensaver-time-out van 10 Minuten**: Standaard inactiviteitstijd gewijzigd naar **10 minuten** (instelbaar: 2, 5, 10, 15, 30 min of Uitgeschakeld).
* **🖱️ Virtuele Muis van Rand tot Rand, Contextuele Menuknop (`☰`) & Versie op Startscherm**: De virtuele cursor in webportalen en HTML5-games bereikt soepel de boven- en onderrand; de **Menuknop (`☰` Drie Lijnen)** opent **Instellingen** op het Startscherm, **IPTV-instellingen** in IPTV en schakelt de **Virtuele Muis** in webportalen/games; betrouwbaar lang indrukken van `OK`; compact **Salsa Store**-icoon; `v3.0`-versie en **automatische GitHub OTA-updates**.
* **🌐 Live 4-Dashboard Centrum, Webportalen & v3.0-engine in 21 Talen**: **Weer**, **Nieuws** (21 talen, 5 rubrieken), **Economie & Markten**, **Voetbal (365Scores)**, standaard webportalen (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), responsieve bovenbalk en snelle 60 FPS v3.0-engine.

### 🖥️ 1. Filmisch en Dynamisch Startscherm
* **Slimme Focus met Zoom:** Kaarten komen tot leven wanneer ze worden geselecteerd met oplichtende randen, zachte schaduwen en dynamische schaling.
* **Contextuele Header & Live Weer:** Toont poëtische begroetingen op basis van het tijdstip van de dag, een realtime klok (12u/24u), netwerkstatus (Wi-Fi / Ethernet) en live lokale weersomstandigheden.
* **Favorieten & Snel Vastmaken:** Maak uw favoriete apps vast aan de snelle toegangsrij of wijzig direct de volgorde.
* **Ondersteuning voor 21 Talen (LTR & RTL):** Volledig gelokaliseerd in **21 talen**, waaronder Nederlands, Engels, Arabisch, Duits, Frans, Italiaans, Russisch, Spaans, Portugees, Turks, Pools, Grieks, Indonesisch, Vietnamees, Thais, Chinees, Japans, Koreaans, Hindi, Urdu en Perzisch.

<div align="center">
  <img src="docs/screenshots/home_screen.png" alt="Home Screen" width="48%"/>
  <img src="docs/screenshots/wallpaper_grid.png" alt="Wallpaper and Grid" width="48%"/>
</div>

---

### 📺 2. Ingebouwde IPTV- en Live TV-speler
* **M3U / M3U8 Afspeellijst-ondersteuning:** Voeg uw eigen afspeellijsten toe of ontdek vooraf geladen wereldwijde tv-zenders.
* **Native ExoPlayer (Media3) Engine:** Razendsnel zappen tussen kanalen met ondersteuning voor HLS- en DASH-streams.
* **Categoriefilter & Zoeken:** Filter zenders op Sport, Nieuws, Films, Kinderen of zoek direct op naam.
* **Favoriete Zenders:** Sla uw meest bekeken zenders op voor toegang met één klik vanaf het startscherm.

<div align="center">
  <img src="docs/screenshots/iptv_player.png" alt="IPTV Player" width="750"/>
</div>

---

### 🛍️ 3. Salsa Store (App Store voor TV)
* **Geselecteerde Catalogus voor Android TV:** Download en installeer de beste streaming-apps, mediaspelers, browsers en hulpprogramma's die zijn geoptimaliseerd voor tv — zonder Google-account.
* **Direct Downloaden & Installeren:** Realtime voortgangsbalken met automatische activering van het pakketinstallatieprogramma.

<div align="center">
  <img src="docs/screenshots/aura_store.png" alt="Salsa Store" width="750"/>
</div>

---

### 🎮 4. Cloud Gaming Portals & Web Apps
* **Gamen Zonder Console:** Directe toegang met één klik tot **Xbox Cloud Gaming**, **NVIDIA GeForce NOW**, **Amazon Luna** en retro/web-gameportals.
* **Geoptimaliseerd voor Controllers:** Verbind uw Bluetooth-controller en verander uw tv in een volledig gamingstation.

<div align="center">
  <img src="docs/screenshots/gaming_portals.png" alt="Gaming Portals" width="750"/>
</div>

---

### 📥 5. Mini Downloader & APK Sideloader via URL/Code
* **Downloaden via URL of Korte Code:** Voer een directe `.apk`-link of korte numerieke code in met het afstandsbedieningsvriendelijke schermtoetsenbord.
* **Downloadgeschiedenis:** Beheer, herinstalleer of verwijder eerder gedownloade APK-bestanden eenvoudig.

<div align="center">
  <img src="docs/screenshots/mini_downloader.png" alt="Mini Downloader" width="750"/>
</div>

---

### 🎨 6. Uitgebreide Personalisatie & Ouderlijk Toezicht
* **4K Achtergronden & Thema's:** Kies uit tientallen haarscherpe achtergronden (Natuur, Ruimte, Abstract, Cyberpunk, Minimalistisch) of stel een eigen afbeelding in vanaf intern geheugen/USB.
* **Bovenbalk Pictogrammodus:** Kies voor het tonen van het **Bedieningspaneel**, sla het Bedieningspaneel over voor **Directe Instellingen**, of **Toon Beide Pictogrammen Samen** op het startscherm.
* **Amerikaanse & Internationale Datumnotaties:** Amerikaans (`MM/dd/yyyy`, `MMMM d, yyyy`, `EEEE, MMMM d, yyyy`, `EEE, MMM d`), Internationaal (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Europees (`dd.MM.yyyy`) of verborgen.
* **UI-schaling, Dynamische Kleuren (Material You) & Alle Android-apps (Sideloaded APKs):** Pas het aantal kolommen en de kaarttransparantie (10%–100%) aan, activeer **Material You Dynamische Kleuren**, en schakel tussen alleen TV-apps of **alle geïnstalleerde Android-apps (inclusief mobiele APK's)**.
* **Filmische Screensaver (`m.youtube.com`), Eigen YouTube-video's & Fotodiavoorstelling (`Slideshow`):** Speel schermvullende video's schoon af via `m.youtube.com` zonder tekst of overlays, voeg uw eigen YouTube-video's toe met **directe live voorvertoning**, of speel foto's af vanuit een map (geheugen/USB) of 4K-galerijen met instelbare duur (`5s`–`5m`), **Ken Burns / Zachte Zoom / Statisch**, 6 overgangen, 7 sfeereffecten (**Sneeuw ❄️**, **Vuurvliegjes ✨**, **Regen 🌧️**, **Herfstbladeren 🍂**, **Sakura 🌸**, **Sterren ⭐**, **Bokeh 🔮**), hoekklok en heldere achtergrondmuziek (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**) of eigen MP3.
* **Ingebouwde App-opslagbeheerder & 10 Systeemsnelkoppelingen (met `Launcher Manager`):** Bekijk alle geïnstalleerde apps gesorteerd op grootte (`MB`), open systeeminfo of **verwijder apps direct** om ruimte vrij te maken, plus 10 snelle systeemsnelkoppelingen (**Launcher Manager**, **Ontwikkelaarsopties**, Wi-Fi, Ethernet, Scherm, Geluid, Datum/Tijd, Alle instellingen, App-info, Opslagbeheer).
* **Dual-Mode DNS-schild (AdBlock + Gezinsbescherming met PIN) & Pincode-slot:** Kies tussen **Advertentie- & Trackerblokkering** of **Gezinsbeschermingsmodus** (**uitschakelen vereist strikt uw 4-cijferige pincode**) en beveilig apps en IPTV-categorieën met een pincode.
* **Universeel Spraakzoeken (`🎤`), Slimme Webbladwijzers & Multi-Playlist IPTV:** Geïntegreerd spraakzoeken, beheer van **meerdere M3U-afspeellijsten**, slimme webbladwijzers met automatische HD-pictogrammen en **YouTube TV Leanback**-modus, rij met recente apps en geverifieerde GitHub OTA-updates.

> ⚠️ **Belangrijke Opmerking over Kinder- / Gezinsbescherming:** Het verlaten van deze modus in de launcher is beveiligd met PIN. **Een launcher alleen kan echter niet voorkomen dat een kind vertrekt via een andere app of de `Home`-knop**, tenzij u **Salsa TV Launcher** instelt als **Standaard Startscherm** en **Systeeminstellingen** vergrendelt met een PIN.

---

## 🛡️ Beveiliging & Transparantie van Machtigingen

* **Standaard HTTPS (`network_security_config.xml`)**: Niet-versleuteld HTTP-verkeer is globaal uitgeschakeld (`cleartextTrafficPermitted="false"`), met gerichte uitzonderingen alleen waar nodig.
* **WebView Safe Browsing Actief**: Google Safe Browsing is ingeschakeld (`EnableSafeBrowsing=true`) zonder telemetrie (`MetricsOptOut=true`).
* **Geverifieerde OTA-updates**: Vóór installatie controleert `AppUpdateManager` de **SHA-256-hash** (`version.json`), de **pakketnaam** en het **APK-handtekeningcertificaat**.
* **Geen brede opslagmachtigingen (`Geen MANAGE_EXTERNAL_STORAGE`)**: Alle lokale bestanden worden veilig geselecteerd via **Storage Access Framework (SAF)**.
* **Uitleg van machtigingen**: `INTERNET`/`ACCESS_NETWORK_STATE` (IPTV, dashboards, Store, OTA), optioneel `ACCESS_COARSE_LOCATION` (grof stadsweer), optioneel `RECORD_AUDIO` (alleen voor Spraakzoeken `🎤`), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES` (alleen op directe gebruikersactie), `BIND_VPN_SERVICE` (lokaal AdGuard DNS-schild) en `RECEIVE_BOOT_COMPLETED`.

---

## 🎮 Sneltoetsengids voor de Afstandsbediening

Salsa TV Launcher is ontworpen om moeiteloos te worden bediend met elke standaard Android TV-afstandsbediening:

| Knop op Afstandsbediening | Waar te gebruiken | Uitgevoerde Actie |
| :--- | :--- | :--- |
| **OK / Selecteren (Kort indrukken)** | Elke app of kaart | Opent de geselecteerde app, zender of portal. |
| **OK / Selecteren (Lang indrukken)** | Op een app- of zendericoon | Opent het **Snelle Opties-menu** (Verplaatsen, Verbergen, Pincode-slot, Spelcategorie, App-info, Verwijderen). |
| **Cijfertoetsen (`0–9`)** | Startscherm • IPTV-lijst • Speler | **Snel Zappen**: typ een zendernummer om er direct naartoe te springen (`OK` bevestigen, `Terug` wijzigen). |
| **Menu-knop (☰ Drie Lijnen)** | Afhankelijk van het scherm | Opent **Instellingen** op het **Startscherm**, opent **IPTV-instellingen** in **IPTV** (zenders & speler; `INFO`/`GUIDE` voor de infobalk) en schakelt de **Virtuele Muis** in **Webportalen & Games**. |
| **Spraak / Google Assistent-knop (🎤)** | Overal in de Launcher | Opent **Universeel Spraakzoeken** van Salsa om apps en IPTV-zenders te zoeken of spraakopdrachten uit te voeren. |
| **Navigatietoetsen (D-Pad)** | Door de hele app | Navigeert soepel tussen categorieën, apps, zenders en afspeelknoppen. |
| **Terug-knop (Back)** | Binnen vensters/spelers | Sluit geopende menu's, verlaat de volledig scherm-speler of keert terug naar de bovenkant van het startscherm. |

> 💡 **Tip:** U kunt deze volledige knoppengids ook altijd direct in de app bekijken via **Instellingen > Afstandsbedieningsgids**.

---

## 🛠️ Architectuur & Technologieën

* **Taal:** 100% [Kotlin](https://kotlinlang.org/) met Coroutines en StateFlow.
* **Gebruikersinterface (UI):** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) + adaptief Material Design 3 voor TV.
* **Architectuur:** MVVM (Model-View-ViewModel) met schone statusscheiding (`UiState`).
* **Media Afspelen:** [AndroidX Media3 / ExoPlayer](https://developer.android.com/guide/topics/media/media3) voor HLS/M3U8-streams.
* **Afbeeldingen Laden:** [Coil](https://coil-kt.github.io/coil/) met geoptimaliseerde schijf- en geheugencache.
* **Netwerk & Weer:** OkHttp + Open-Meteo API voor nauwkeurige weergegevens zonder API-sleutel.

---

## 📦 Installatie

> ℹ️ **Platformopmerking**: Salsa TV Launcher is primair ontworpen voor **Android TV, Google TV en TV Boxen** met afstandsbediening (`D-Pad`). *(Ondersteuning voor kleine mobiele/touchscreens is momenteel **experimenteel / in ontwikkeling**).*

### Methode 1: Directe APK Download (Aanbevolen)
1. Download het bestand [`salsa-tv-launcher.apk`](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk).
   👉 **[Downloader Korte Code / Directe Link: aftv.news/5157967](http://aftv.news/5157967)**
2. Zet het bestand over naar uw Android TV via een USB-stick, *Send Files to TV* of de *Downloader*-app.
3. Installeer de APK en verleen de gevraagde machtigingen.
4. Druk op de **Home-knop** op uw afstandsbediening en selecteer **Salsa TV Launcher** als uw standaard startscherm (**"Altijd"**).

### Methode 2: Installatie via ADB
```bash
adb connect <IP_VAN_UW_TV>:5555
adb install -r salsa-tv-launcher.apk
```

---

## 📄 Licentie & Auteursrecht

Ontworpen en ontwikkeld door **© Osama Salsa**.  
Deze repository host de officieel gecompileerde release-APK, meertalige documentatie, screenshots en geverifieerde OTA-updatemanifesten (`version.json`).
