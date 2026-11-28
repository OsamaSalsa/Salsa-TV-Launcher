[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | **Polski** | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 🌌 Salsa TV Launcher

**Odkryj przyszłość domowej rozrywki dzięki nowoczesnemu, ultraszybkiemu i kinowemu interfejsowi TV.**

[![Android](https://img.shields.io/badge/Platform-Android%20TV-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.1-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Download APK](https://img.shields.io/badge/Download-APK%20Release-FF0055?style=for-the-badge&logo=github)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

### 📥 [Kliknij tutaj, aby pobrać najnowszą wersję APK (Gotową do instalacji)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

<img src="docs/screenshots/home.png" alt="Salsa TV Launcher Main Preview" width="800"/>

</div>

---

## ✨ Przegląd

**Salsa TV Launcher** to nie kolejny zwykły ekran główny — to kompletny ekosystem rozrywki zaprojektowany od podstaw dla **Android TV oraz przystawek Smart TV Box**. Stworzony w 100% w języku **Kotlin** z wykorzystaniem **Jetpack Compose**, Salsa łączy płynną, kinową estetykę „Glassmorphism” z potężnymi wbudowanymi narzędziami, takimi jak natywny odtwarzacz **IPTV / Telewizji na żywo**, **Centrum Gier w Chmurze**, **Sklep z aplikacjami Salsa Store** oraz **Mini Downloader** do bezpośredniej instalacji pakietów APK.

Zaprojektowany specjalnie pod obsługę pilotem telewizyjnym (nawigacja D-Pad), każdy element fokusu, animacja i przejście działa płynnie w 60 FPS bez przycięć.

---

## 🚀 Główne Funkcje

### 🆕 Nowości w wersji 3.1
* **🧸 Tryb Dzieci (`Kids Mode`) i 4 Motywy z GitHuba (`Tęczowy Jednorożec`, `Magiczny Ogród`, `Kosmiczny Odkrywca`, `Dżungla Dinozaurów`)**: Twórz profile dziecięce zabezpieczone **obowiązkowym 4-cyfrowym kodem PIN** (ustawianym przy tworzeniu) do otwierania Ustawień i menu edycji. Oferuje radosny układ z górnym paskiem zawierającym **tylko Ustawienia** (Zegar, Pogoda, Data i przycisk Ustawień), czcionkami **Playpen Sans Arabic** / **Comic Relief**, oryginalnymi ikonami aplikacji i banerami TV bez zmian, automatycznie włączoną **Ochroną Rodzinną DNS** oraz **4 motywami dziecięcymi**, których tapety `1920×1080` pobierają się bezpośrednio z GitHuba (`/kids_themes/`).
* **🛒 100% Katalog na Żywo w Salsa Store z `auratv.freedev.app` (Zero Zapisanych Nazw Aplikacji)**: Usunięto wszystkie lokalnie zapisane listy aplikacji — **Salsa Store** za każdym razem pobiera nazwy aplikacji, ikony, opisy i linki bezpośrednio na żywo z `https://auratv.freedev.app/index.html`, prezentując je w natywnym interfejsie kategorii TV: **Przeglądarki internetowe** na pierwszym miejscu, następnie **Media społecznościowe**, a potem pozostałe działy.
* **⌨️ Automatyczna Klawiatura Ekranowa dla Pól Tekstowych i Wyszukiwania na Stronach WWW (Poza YouTube)**: Kliknięcie dowolnego pola wyszukiwania lub wprowadzania tekstu (`<input>`, `<textarea>`, `contenteditable` lub pole szukania) na stronach i w aplikacjach webowych automatycznie otwiera natywny pasek wprowadzania oraz domyślną klawiaturę ekranową Android TV i wpisuje tekst bezpośrednio na stronie (z wyłączeniem **YouTube**, który zachowuje własną klawiaturę).
* **🔐 System PIN dla Każdego Profilu, Podwójne Potwierdzenie, Blokada Wejścia i `Master PIN` Administratora**: Usunięto domyślny kod PIN (`1234`). Przed zablokowaniem aplikacji lub sekcji należy utworzyć 4-cyfrowy PIN, **wpisując go dwukrotnie w celu potwierdzenia**. PIN jest zawsze ukryty (`••••`), zmiana lub usunięcie wymaga podania starego kodu, każdy profil posiada opcję **Zablokuj wejście do profilu kodem PIN**, a profil **`Main` jest nieusuwalnym Administratorem (`Admin`)**, którego kod działa jako **`Master PIN Code`** pozwalający wejść na każdy profil i zmienić jego ustawienia w razie zapomnienia kodu.
* **🎙️ Ulepszone Wyszukiwanie Głosowe Android TV**: Bezpośrednie połączenie z usługą rozpoznawania mowy Android TV (`katniss`) z dłuższym czasem nasłuchu mikrofonu pilota Bluetooth i automatycznym ponawianiem.
* **🏠 Wbudowany `Launcher Manager` (Domyślny HOME przez lokalne ADB — bez zewnętrznego PC ani telefonu)**: Ustaw Salsa TV Launcher jako stały domyślny ekran główny HOME bezpośrednio z aplikacji przez lokalne ADB (`127.0.0.1:5555`) z zapisanym kluczem RSA, 3-etapowym kreatorem (`cmd package set-home-activity` z zapasowym `pm`), natychmiastową weryfikacją, przywracaniem fabrycznego launchera w 1 kliknięcie oraz systemowym oknem wyboru (**Ustawienia > System i skróty TV > Launcher Manager**).
* **👤 Profile Wielu Użytkowników, Dynamiczny Kolor Akcentu Motywu i Izolacja Ustawień (`Profiles / Users`)**: Twórz niezależne profile dla domowników — każdy z własnym **Dynamicznym Kolorem Akcentu Motywu** (8 kontrastowych odcieni dopasowanych do TV, które dynamicznie zastępują żółty/złoty kolor w powitaniu, ikonach sekcji Ekranu Głównego [Aplikacje, Gry, Web, IPTV], Ustawieniach, IPTV i obramowaniach fokusu), własnym **Wyglądem i Tapetą**, **Wygaszaczem Ekranu**, **Językiem**, **Ulubionymi i Kolejnością IPTV**, **Kolejnością Aplikacji / Web / Gier**, opcjonalnym **4-cyfrowym PIN-em Profilu** oraz **kontrolą widoczności sekcji** (Pokaż, Zablokuj PIN-em lub Ukryj **IPTV**, **Aplikacje**, **Web** lub **Gry**).
* **🔢 Szybkie Przełączanie Kanałów Cyframi (`0–9`) i Kontrastowe Numery Kanałów**: Wpisz numer kanału z pilota na Ekranie Głównym, w liście IPTV lub w odtwarzaczu, aby natychmiast przełączyć kanał — wspierane czytelnymi, kontrastowymi plakietkami numerów (czarne cyfry na półprzezroczystym białym tle).
* **🎞️ Lokalny Wygaszacz Wideo z Urządzenia / USB (`Media3 / ExoPlayer`)**: Oprócz czystych strumieni pełnoekranowych `m.youtube.com` bez żadnych nakładek, wybierz dowolny lokalny plik wideo z pamięci TV lub pendrive'a USB przez bezpieczny selektor bez uprawnień (`SAF`) dla płynnego zapętlania na pełnym ekranie.
* **📡 Wygaszacz Tekstowy Broadcast (12 Animowanych Motywów, Ciemne Dynamiczne Tła i Lektor Głosowy)**: 12 motywów studyjnych — 6 klasycznych oraz 6 nowych motywów kreatywnych (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — z unikalną animacją, układem oraz **dedykowanym ciemnym dynamicznym tłem z kontrastowymi cieniami** + podglądy na żywo w Ustawieniach, edytowalne wiersze (`3–15s`), **Lektor Głosowy (TTS)** czekający na zakończenie czytania przed zmianą oraz opcjonalna muzyka tła lub lokalny plik MP3.
* **📺 Inteligentne Odzyskiwanie Błędów IPTV (`Następny Kanał` + 15s) i Sekretny Prezent 3× OK**: Gdy kanał IPTV nie działa, wyświetla przejrzysty ekran z jednym przyciskiem (**Następny Kanał**) oraz **15-sekundowym odliczaniem**, które automatycznie ponawia próbę. Dodatkowo 3× `OK` na pustej karcie IPTV odblokowuje wbudowane listy z animacją prezentu.
* **🛒 Top Kategorie Salsa Store i Czysty Wygaszacz Wideo `m.youtube.com`**: **Salsa Store** umieszcza **Przeglądarki WWW** i **Media Społecznościowe** na początku katalogu 9 kategorii sprawdzonych narzędzi i odtwarzaczy Android TV (z trybem offline); wygaszacz wideo odtwarza płynnie na pełnym ekranie przez `m.youtube.com` bez żadnych napisów ani nakładek.
* **🎨 Ustawienia UI na Żywo (`Live UI Settings`) — Bezpośredni Wybór Formatu Zegara**: Pływający pasek na ekranie głównym zawiera teraz osobny przełącznik **Formatu Zegara** (**`24h • 22:00`** / **`12h • AM/PM`**) obok rozmiaru zegara i formatu daty, wraz z podglądem na żywo tapet 4K, jasności, położenia aplikacji, siatki, przezroczystości, powitań, pogody, trybu nocnego, Material You i trybu lekkiego.
* **🖥️ Wygaszacz Paneli, Lektor Wiadomości i Ujednolicona Czysta Muzyka**: Wyświetlaj **Pogodę**, **Wiadomości**, **Ekonomię**, **Piłkę nożną** lub **Automatyczną zmianę**; wygaszacz Wiadomości wspiera **Lektora (TTS)** czytającego wszystkie 6 wiadomości przed przełączeniem, a wszystkie panele korzystają z tych samych krystalicznie czystych utworów (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambient i MP3) z automatycznym ściszaniem podczas czytania.
* **🌙 Domyślny Czas Wygaszacza 10 Minut**: Domyślny czas bezczynności zmieniono na **10 minut** (konfigurowalny: 2, 5, 10, 15, 30 min lub Wyłączony).
* **🖱️ Wirtualna Mysz od Krawędzi do Krawędzi, Kontekstowy Przycisk Menu (`☰`) i Wersja na Ekranie Startowym**: Kursor w portalach webowych i grach HTML5 sięga samej górnej i dolnej krawędzi; przycisk **Menu (`☰` Trzy Linie)** otwiera **Ustawienia** na ekranie głównym, **Ustawienia IPTV** w IPTV i przełącza **Wirtualną Mysz** w portalach webowych/grach; niezawodne długie przytrzymanie `OK`; odświeżona ikona **Salsa Store**; napis `v3.0` i **automatyczne aktualizacje GitHub OTA**.
* **🌐 Centrum 4 Paneli na Żywo, Portale Web i Silnik v3.0 w 21 Językach**: **Pogoda**, **Wiadomości** (21 języków, 5 działów), **Ekonomia i Rynki**, **Centrum Piłkarskie (365Scores)**, domyślne portale (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), elastyczny górny pasek i lekki silnik v3.0 60 FPS.

### 🖥️ 1. Kinowy i Dynamiczny Ekran Główny
* **Inteligentny Fokus i Powiększenie:** Karty ożywają po najechaniu dzięki świecącym obramowaniom, miękkim cieniom i dynamicznemu skalowaniu.
* **Kontekstowy Nagłówek i Pogoda na Żywo:** Wyświetla poetyckie powitania dopasowane do pory dnia, zegar czasu rzeczywistego (12h/24h), stan sieci (Wi-Fi / Ethernet) oraz aktualną lokalną pogodę.
* **Ulubione i Szybkie Przypinanie:** Przypinaj ulubione aplikacje do paska szybkiego dostępu lub błyskawicznie zmieniaj ich kolejność.
* **Obsługa 21 Języków (LTR i RTL):** Pełna lokalizacja w **21 językach**, w tym polskim, angielskim, arabskim, niemieckim, francuskim, włoskim, rosyjskim, hiszpańskim, portugalskim, tureckim, niderlandzkim, greckim, indonezyjskim, wietnamskim, tajskim, chińskim, japońskim, koreańskim, hindi, urdu oraz perskim.

<div align="center">
  <img src="docs/screenshots/home_screen.png" alt="Home Screen" width="48%"/>
  <img src="docs/screenshots/wallpaper_grid.png" alt="Wallpaper and Grid" width="48%"/>
</div>

---

### 📺 2. Wbudowany Odtwarzacz IPTV i Telewizji na Żywo
* **Obsługa List M3U / M3U8:** Dodawaj własne listy odtwarzania lub przeglądaj wstępnie załadowane kanały z całego świata.
* **Natywny Silnik ExoPlayer (Media3):** Błyskawiczne przełączanie kanałów ze wsparciem dla strumieni HLS i DASH.
* **Filtrowanie Kategorii i Wyszukiwanie:** Filtruj kanały według kategorii (Sport, Wiadomości, Filmy, Dla dzieci) lub wyszukuj je natychmiastowo.
* **Ulubione Kanały:** Zapisuj najczęściej oglądane kanały, aby uruchamiać je jednym kliknięciem z ekranu głównego.

<div align="center">
  <img src="docs/screenshots/iptv_player.png" alt="IPTV Player" width="750"/>
</div>

---

### 🛍️ 3. Salsa Store (Sklep z Aplikacjami na TV)
* **Wyselekcjonowany Katalog Android TV:** Pobieraj i instaluj najlepsze aplikacje streamingowe, odtwarzacze multimedialne, przeglądarki i narzędzia zoptymalizowane pod telewizor — bez konieczności logowania na konto Google.
* **Bezpośrednie Pobieranie i Instalacja:** Paski postępu w czasie rzeczywistym z automatycznym uruchamianiem systemowego instalatora pakietów.

<div align="center">
  <img src="docs/screenshots/aura_store.png" alt="Salsa Store" width="750"/>
</div>

---

### 🎮 4. Portale Gier w Chmurze i Aplikacje Webowe
* **Graj Bez Konsoli:** Bezpośredni dostęp jednym kliknięciem do **Xbox Cloud Gaming**, **NVIDIA GeForce NOW**, **Amazon Luna** oraz portali z grami retro.
* **Wsparcie dla Kontrolerów:** Podłącz kontroler Bluetooth i zamień swój telewizor w pełnoprawne centrum gier.

<div align="center">
  <img src="docs/screenshots/gaming_portals.png" alt="Gaming Portals" width="750"/>
</div>

---

### 📥 5. Mini Downloader i Instalator APK z URL/Kodu
* **Pobieranie przez URL lub Krótki Kod:** Wpisz dowolny bezpośredni link do pliku `.apk` lub krótki kod numeryczny za pomocą wygodnej klawiatury ekranowej.
* **Historia Pobrań:** Zarządzaj, instaluj ponownie lub usuwaj wcześniej pobrane pliki APK.

<div align="center">
  <img src="docs/screenshots/mini_downloader.png" alt="Mini Downloader" width="750"/>
</div>

---

### 🎨 6. Zaawansowana Personalizacja i Kontrola Rodzicielska
* **Tapety 4K i Motywy:** Wybieraj spośród dziesiątek tapet wysokiej rozdzielczości (Natura, Kosmos, Abstrakcja, Cyberpunk, Minimalizm) lub ustaw własny obraz z pamięci wewnętrznej/USB.
* **Tryb Ikony Górnego Paska:** Wybierz wyświetlanie ikony **Centrum Sterowania**, pominięcie Centrum Sterowania i wyświetlenie ikony **Bezpośrednich Ustawień**, lub **Wyświetlanie Obu Ikon Razem** na Ekranie Głównym.
* **Amerykańskie i Międzynarodowe Formaty Daty:** Format amerykański (`MM/dd/yyyy`, `MMMM d, yyyy`, `EEEE, MMMM d, yyyy`, `EEE, MMM d`), międzynarodowy (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), europejski (`dd.MM.yyyy`) lub ukryty.
* **Skalowanie Interfejsu, Dynamiczne Kolory (Material You) i Aplikacje Spoza TV (Sideloaded APKs):** Dostosuj liczbę kolumn, przezroczystość kart (10%–100%), włącz **Dynamiczne Kolory Material You** oraz przełączaj między wyświetlaniem tylko aplikacji TV a **wszystkimi zainstalowanymi aplikacjami Android (w tym mobilnymi APK)**.
* **Kinowy Wygaszacz Ekranu (`m.youtube.com`), Własne Filmy YouTube i Pokaz Slajdów (`Slideshow`):** Odtwarza wideo na pełnym ekranie przez `m.youtube.com` bez napisów ani nakładek, umożliwia dodawanie własnych filmów YouTube z **natychmiastowym podglądem**, lub wyświetlanie zdjęć z folderu (Pamięć/USB) bądź galerii 4K z regulowanym czasem (`5s`–`5m`), ruchem **Ken Burns / Łagodny Zoom / Statyczny**, 6 przejściami, 7 efektami cząsteczkowymi (**Śnieg ❄️**, **Świetliki ✨**, **Deszcz 🌧️**, **Jesienne Liście 🍂**, **Sakura 🌸**, **Gwiazdy ⭐**, **Bokeh 🔮**), zegarem w rogu oraz czystą muzyką w tle (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**) lub własnym MP3.
* **Wbudowany Menedżer Pamięci Aplikacji i 10 Skrótów Systemowych (z `Launcher Manager`):** Przeglądaj zainstalowane aplikacje posortowane według rozmiaru (`MB`), otwieraj informacje systemowe lub **odinstalowuj aplikacje bezpośrednio**, aby zwolnić miejsce; korzystaj z 10 szybkich skrótów (**Launcher Manager**, **Opcje programisty**, Wi-Fi, Ethernet, Ekran, Dźwięk, Data i czas, Ustawienia, O aplikacji, Pamięć).
* **Dwutrybowy Filtr DNS (AdBlock + Ochrona Rodzinna z PIN) i Blokada PIN:** Wybierz między **blokowaniem reklam i elementów śledzących** a **Trybem Ochrony Rodzinnej** (**wyjście z Trybu Rodzinnego wymaga podania 4-cyfrowego kodu PIN**) i chroń aplikacje oraz kategorie IPTV kodem PIN.
* **Wyszukiwanie Głosowe (`🎤`), Inteligentne Zakładki Web i Multi-Lista IPTV:** Zintegrowane wyszukiwanie głosowe, zarządzanie **wieloma listami M3U**, zakładki webowe z ikonami HD i trybem **YouTube TV Leanback**, pasek ostatnich aplikacji oraz zweryfikowane aktualizacje OTA z GitHub.

> ⚠️ **Ważna Uwaga o Trybie Dziecięcym / Ochrony Rodzinnej:** Wyłączenie trybu w launcherze chroni kod PIN. **Sam launcher nie zapobiegnie jednak wyjściu dziecka przez inną aplikację lub przycisk `Home`**, o ile nie ustawisz **Salsa TV Launcher** jako **Domyślnego Ekranu Głównego** oraz nie zablokujesz **Ustawień Systemowych** kodem PIN.

---

## 🛡️ Bezpieczeństwo i Przejrzystość Uprawnień

* **Domyślnie HTTPS (`network_security_config.xml`)**: Nieszyfrowany ruch HTTP jest globalnie wyłączony (`cleartextTrafficPermitted="false"`), z minimalnymi wyjątkami wyłącznie dla niezbędnych usług.
* **Aktywne Bezpieczne Przeglądanie WebView**: Włączona ochrona Google Safe Browsing (`EnableSafeBrowsing=true`) bez wysyłania telemetrii (`MetricsOptOut=true`).
* **Zweryfikowane Aktualizacje OTA**: Przed instalacją aktualizacji z GitHub `AppUpdateManager` weryfikuje sumę **SHA-256** (`version.json`), **nazwę pakietu** oraz **certyfikat podpisu APK**.
* **Brak szerokich uprawnień pamięci (`Brak MANAGE_EXTERNAL_STORAGE`)**: Wszystkie lokalne pliki są wybierane przez bezpieczny selektor **Storage Access Framework (SAF)**.
* **Wyjaśnienie uprawnień**: `INTERNET`/`ACCESS_NETWORK_STATE` (IPTV, panele, Store, OTA), opcjonalne `ACCESS_COARSE_LOCATION` (przybliżona pogoda miasta), opcjonalne `RECORD_AUDIO` (tylko Wyszukiwanie Głosowe `🎤`), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES` (wyłącznie na wyraźne polecenie użytkownika), `BIND_VPN_SERVICE` (lokalna tarcza AdGuard DNS) oraz `RECEIVE_BOOT_COMPLETED`.

---

## 🎮 Przewodnik po Przyciskach Pilota

Salsa TV Launcher został zaprojektowany do intuicyjnej obsługi dowolnym standardowym pilotem Android TV:

| Przycisk na Pilocie | Gdzie Użyć | Wykonywana Akcja |
| :--- | :--- | :--- |
| **Przycisk OK / Wybierz (Krótkie naciśnięcie)** | Dowolna aplikacja lub karta | Otwiera wybraną aplikację, kanał lub portal. |
| **Przycisk OK / Wybierz (Długie przytrzymanie)** | Na ikonie aplikacji lub kanału | Otwiera **Menu Szybkich Opcji** (Przenieś, Ukryj, Zablokuj PIN-em, Kategoria gier, Informacje, Odinstaluj). |
| **Klawisze Cyfrowe (`0–9`)** | Ekran Główny • Lista IPTV • Odtwarzacz | **Szybkie Przełączanie Kanałów**: wpisz numer kanału, aby przejść do niego bezpośrednio (`OK` zatwierdza, `Wstecz` poprawia). |
| **Przycisk Menu (☰ Trzy Linie)** | Zależnie od ekranu | Na **Ekranie Głównym** otwiera **Ustawienia**, w **IPTV** (lista i odtwarzacz) otwiera **Ustawienia IPTV** (`INFO`/`GUIDE` przełącza pasek informacji), a w **Portalach Web / Grach** przełącza **Wirtualną Mysz**. |
| **Przycisk Głosu / Asystent Google (🎤)** | W dowolnym miejscu Launchera | Otwiera **Uniwersalne Wyszukiwanie Głosowe** Salsa do szukania aplikacji, kanałów IPTV lub wydawania poleceń głosowych. |
| **Przyciski Kierunkowe (D-Pad)** | W całej aplikacji | Płynna nawigacja między kategoriami, aplikacjami, kanałami i elementami sterowania. |
| **Przycisk Wstecz (Back)** | W oknach / odtwarzaczu | Zamyka otwarte menu, wychodzi z pełnego ekranu lub wraca na górę ekranu głównego. |

> 💡 **Wskazówka:** Ten przewodnik jest zawsze dostępny bezpośrednio w aplikacji w sekcji **Ustawienia > Przewodnik po pilocie**.

---

## 🛠️ Architektura i Technologie

* **Język:** 100% [Kotlin](https://kotlinlang.org/) z Coroutines i StateFlow.
* **Interfejs (UI):** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material Design 3 dla TV.
* **Architektura:** MVVM (Model-View-ViewModel) z czystym zarządzaniem stanem (`UiState`).
* **Odtwarzacz Multimediów:** [AndroidX Media3 / ExoPlayer](https://developer.android.com/guide/topics/media/media3) dla strumieni HLS/M3U8.
* **Ładowanie Obrazów:** [Coil](https://coil-kt.github.io/coil/) ze zoptymalizowaną pamięcią podręczną.
* **Sieć i Pogoda:** OkHttp + Open-Meteo API zapewniające dokładne dane pogodowe bez klucza API.

---

## 📦 Instalacja

> ℹ️ **Uwaga o Platformie**: Salsa TV Launcher jest zaprojektowany przede wszystkim dla **Android TV, Google TV i TV Boxów** z pilotem (`D-Pad`). *(Obsługa małych ekranów mobilnych/dotykowych ma obecnie status **eksperymentalny / w trakcie rozwoju**).*

### Metoda 1: Bezpośrednie Pobranie APK (Zalecane)
1. Pobierz plik [`salsa-tv-launcher.apk`](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk).
   👉 **[Krótki kod aplikacji Downloader / Bezpośredni link: aftv.news/5157967](http://aftv.news/5157967)**
2. Prześlij plik na swój telewizor Android TV za pomocą pamięci USB, aplikacji *Send Files to TV* lub *Downloader*.
3. Zainstaluj plik APK i przyznaj wymagane uprawnienia.
4. Naciśnij przycisk **Home** na pilocie i wybierz **Salsa TV Launcher** jako domyślny ekran główny (**„Zawsze”**).

### Metoda 2: Instalacja przez ADB
```bash
adb connect <ADRES_IP_TV>:5555
adb install -r salsa-tv-launcher.apk
```

---

## 📄 Licencja i Prawa Autorskie

Zaprojektowany i stworzony przez **© Osama Salsa**.  
To repozytorium zawiera oficjalnie skompilowany plik APK, wielojęzyczną dokumentację, zrzuty ekranu oraz zweryfikowane manifesty automatycznych aktualizacji OTA (`version.json`).
