[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | **Italiano** | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.0

![Schermata Principale](docs/screenshots/home.png)

**Un launcher e media center per Android TV moderno, ultra-veloce, leggero, completamente personalizzabile e al 100% senza pubblicità sviluppato con Jetpack Compose.**  
*La soluzione All-in-One che sostituisce dozzine di app separate sul tuo televisore.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La soluzione All-in-One

La maggior parte dei launcher TV attuali è piena di annunci forzati, pesanti anteprime video e menu lenti.

**Salsa TV Launcher** è concepito per essere l'esatto opposto: un media center pulito, scattante, leggero e privo di qualsiasi pubblicità o telemetria.

### ✨ 7 moduli integrati fondamentali
1. **🚀 Launcher ultra-veloce e leggero**: Avvio immediato, precaricamento parallelo delle icone su processori multi-core e navigazione fluida a 60 FPS.
2. **📺 Player IPTV con canali preferiti in home**: Supporto playlist M3U e avvio con 1 clic dei canali preferiti.
3. **🎥 MiniTube**: Esperienza YouTube Leanback ottimizzata per TV con riproduzione ospite rapida.
4. **📥 Mini Downloader**: Downloader ad alta velocità per file e APK con installatore automatico intelligente.
5. **🛒 Salsa Store**: Negozio web integrato per esplorare e installare comodamente app per Android TV.
6. **🕹️ 4 portali di giochi online**: Migliaia di giochi HTML5 istantanei senza download (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Blocco pubblicità a livello di sistema**: Protezione AdGuard DNS integrata per bloccare annunci e tracker su tutto il dispositivo.

---

## 🌟 Nuove funzionalità e ultimi miglioramenti

### 🆕 Novità della Versione 3.1
* **🛒 Catalogo `Salsa Store` 100% Live (`https://auratv.freedev.app/index.html`) e Categorie TV Native**: Nessun nome di app è memorizzato nell'applicazione — a ogni apertura **Salsa Store** carica in tempo reale nomi e informazioni da `https://auratv.freedev.app/index.html` nel layout nativo per categorie, con **Browser web** come prima categoria, seguito da **Social media** e dalle restanti categorie.
* **⌨️ Tastiera a Schermo Automatica per Campi di Testo e Ricerca Web (Escluso YouTube)**: Cliccando su qualsiasi casella di testo o barra di ricerca nei siti web si apre subito la tastiera predefinita di sistema per digitare (ad eccezione di **YouTube**, che utilizza la propria tastiera integrata).
* **🔐 Sistema PIN per Profilo, Doppia Conferma Mascherata, Blocco Accesso Profilo & `Master PIN` Admin**: Rimosso qualsiasi PIN predefinito (`1234` non esiste più). Prima di bloccare app, sezioni o categorie IPTV è necessario creare un PIN a 4 cifre **inserendolo due volte per conferma**. Il PIN è sempre mascherato (`••••`), la modifica o rimozione richiede il PIN attuale, ogni profilo dispone del **Blocco accesso con PIN**, e il profilo **`Main` è l'Amministratore (`Admin`)** (non eliminabile) il cui PIN funge da **`Master PIN Code`** per accedere o modificare qualsiasi profilo in caso di PIN dimenticato.
* **🎙️ Ricerca Vocale Android TV Più Affidabile**: Collegamento diretto al servizio vocale Android TV (`katniss`) con maggiore tolleranza per il microfono del telecomando Bluetooth e nuovo tentativo automatico.
* **🏠 `Launcher Manager` Integrato (Impostazione HOME predefinito via ADB locale — senza PC o telefono)**: Imposta Salsa TV Launcher come launcher HOME permanente direttamente dall'app tramite ADB locale (`127.0.0.1:5555`) con chiave RSA persistente, procedura guidata in 3 passaggi (`cmd package set-home-activity` con fallback `pm`), verifica immediata, ripristino del launcher originale in 1 clic e selettore di sistema di emergenza (**Impostazioni > Sistema e scorciatoie TV > Launcher Manager**).
* **👤 Profili Multi-Utente, Colore di Accento Dinamico per Utente & Isolamento (`Profiles / Users`)**: Crea più profili familiari indipendenti — ciascuno con il proprio **Colore di Accento Dinamico del Tema** (8 tonalità ad alto contrasto calibrate per TV che sostituiscono dinamicamente il giallo/oro nel saluto, nelle icone della Home [App, Giochi, Web, IPTV], nelle Impostazioni, in IPTV e in tutti i bordi di focus), il proprio **Aspetto e Sfondo**, **Salvaschermo**, **Lingua**, **Preferiti e Ordine IPTV**, **Ordine App / Web / Giochi**, **PIN Profilo opzionale a 4 cifre** e **controllo per sezione** (Mostra, Blocca con PIN o Nascondi **IPTV**, **App**, **Web** o **Giochi**).
* **🔢 Cambio Rapido Canale con Tasti Numerici (`0–9`) e Badge ad Alto Contrasto**: Digita un numero di canale sul telecomando dalla Home, dalla lista IPTV o dal player live per passare subito al canale desiderato, con badge numerici nitidi ad alto contrasto (cifre nere su sfondo bianco traslucido).
* **🎞️ Salvaschermo Video Locale da Dispositivo / USB (`Media3 / ExoPlayer`)**: Oltre agli streaming puliti a schermo intero via `m.youtube.com` senza sovrapposizioni, seleziona qualsiasi video locale dalla memoria TV o da chiavetta USB tramite il selettore sicuro senza permessi (`SAF`) per la riproduzione fluida in loop.
* **📡 Salvaschermo Testo Broadcast (12 Temi Animati, Sfondi Scuri Dinamici e Lettore Vocale)**: 12 temi studio unici — 6 classici più 6 nuovi temi creativi (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — con animazioni, layout e **sfondi scuri dinamici dedicati a ogni tema** con ombre colorate cangianti + anteprime live nelle Impostazioni, righe modificabili (`3–15s`), **Lettore Vocale (TTS)** che attende la fine della lettura prima di avanzare, e musica ambientale o MP3 locale opzionale.
* **📺 Ripristino Intelligente Errori IPTV (`Canale Successivo` + 15s) & Regalo Segreto 3× OK**: Quando un canale IPTV non si avvia, mostra una schermata essenziale con un solo pulsante (**Canale Successivo**) e un **conto alla rovescia di 15 secondi** che ritenta automaticamente lo stesso canale. Inoltre, premendo `OK` 3 volte sulla scheda IPTV vuota si sbloccano le playlist integrate con un'animazione celebrativa.
* **🛒 Categorie Top Salsa Store & Salvaschermo Video Pulito `m.youtube.com`**: **Salsa Store** posiziona **Browser Web** e **Social Media** in cima al catalogo da 9 categorie di utilità e player Android TV verificati (con fallback offline); il salvaschermo video riproduce sempre a schermo intero via `m.youtube.com` senza testi né overlay sullo schermo.
* **🎨 Impostazioni UI Live (`Live UI Settings`) — Formato Ora Diretto**: La barra trasparente sulla Home ora include un selettore diretto del **Formato Ora** (**`24h • 22:00`** vs **`12h • AM/PM`**) accanto a dimensione orologio e formato data, insieme all'anteprima in tempo reale per sfondi 4K, luminosità, offset app, griglia, opacità, saluti, meteo, filtro notte, Material You e modalità leggera.
* **🖥️ Salvaschermo Pannelli Live, Lettore Vocale & Musica Unificata Cristallina**: Imposta **Meteo**, **Notizie**, **Economia**, **Calcio** o **Rotazione automatica**; il salvaschermo Notizie supporta la **lettura vocale (TTS)** completa dei 6 titoli prima di cambiare schermata, e tutti i pannelli condividono gli stessi brani musicali nitidi (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambient e MP3) con riduzione automatica durante il parlato.
* **🌙 Timeout Predefinito Salvaschermo di 10 Minuti**: L'inattività predefinita è ora di **10 minuti** (configurabile: 2, 5, 10, 15, 30 min o Disattivato).
* **🖱️ Mouse Virtuale Bordo-a-Bordo, Tasto Menu Contestuale (`☰`) & Versione Avvio**: Il cursore virtuale nei portali web e giochi raggiunge perfettamente i bordi superiore e inferiore; il tasto **Menu (`☰` Tre linee)** apre le **Impostazioni** nella Home, le **Impostazioni IPTV** in IPTV e attiva/disattiva il **Mouse Virtuale** nei portali web/giochi; pressione prolungata affidabile su `OK`; icona compatta **Salsa Store**; indicazione `v3.0` e **aggiornamenti automatici OTA via GitHub**.
* **🌐 Hub 4 Pannelli Live, Portali Web & Motore v3.0 in 21 Lingue**: Accesso diretto a **Meteo**, **Notizie** (21 lingue, 5 sezioni), **Economia e Mercati** e **Calcio (365Scores)**, portali web predefiniti (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), barra superiore adattiva e motore v3.0 a 60 FPS.

### ⚡ Prestazioni estreme e risposta immediata
* **Caricamento immediato di icone e banner**: Tutte le app vengono decodificate in background su tutti i core della CPU, apparendo all'istante all'avvio.
* **Navigazione D-Pad fulminea (110 ms)**: Accelerazione hardware tramite `graphicsLayer` e transizioni a 110 ms per eliminare qualsiasi ritardo del telecomando.
* **Effetto alone ambientale ottimizzato GPU**: Sostituzione dei filtri di sfocatura pesanti con gradienti radiali nativi, garantendo 60 FPS stabili.
* **Schermata iniziale pulita (Pulsante Edit inferiore rimosso)**: Riorganizzazione immediata tramite **pressione prolungata di `OK`**.

### 🌙 Salvaschermo intelligente e gestione del sistema
* **Passaggio automatico al salvaschermo esterno (`Disattivato`)**: Disattivando il salvaschermo interno, il launcher rilascia il blocco schermo (`FLAG_KEEP_SCREEN_ON`), consentendo al salvaschermo di sistema o ad app esterne di avviarsi liberamente.
* **Salvaschermo integrato immersivo (`Attivo`)**: Con timer a 2, 5, 10, 15 o 30 minuti, offre tre stili distinti:
  * **Orologio e data**: Grande orologio luminoso con protezione burn-in per pannelli TV.
  * **Sfondo 4K con info in basso**: Gradiente a 45° che risalta orario, data e messaggio di benvenuto.
  * **Video cinematografico a schermo intero via `m.youtube.com` e video personalizzati**: Camini caldi, chalet innevati o cascate riprodotti in modo pulito via `m.youtube.com` senza testi né sovrapposizioni, con possibilità di **aggiungere video YouTube personalizzati**, ripristinare i preset e avviare un'**anteprima immediata** dalle Impostazioni.
  * **Presentazione foto (`Slideshow`)**: Mostra le foto da una cartella su memoria/USB, foto selezionate o gallerie 4K con durata regolabile (`5s`–`5m`), movimento **Ken Burns / Zoom delicato / Statico**, 6 transizioni di ingresso, effetti particellari (**Neve ❄️**, **Lucciole ✨**, **Pioggia 🌧️**, **Foglie d'autunno 🍂**, **Sakura 🌸**, **Stelle ⭐**, **Bokeh 🔮**), orologio in angolo e brani musicali nitidi (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**) o MP3 personalizzato.

### 🕒 Personalizzazione totale di orologio, data e saluti
* **Tipografia multilingue (21 lingue)**: Traduzione completa in **Italiano**, **Arabo (Font Cairo)**, **Inglese**, **Tedesco**, **Francese**, **Spagnolo**, **Russo**, **Portoghese**, **Turco**, **Polacco**, **Olandese**, **Greco**, **Indonesiano**, **Vietnamita**, **Thailandese**, **Cinese**, **Giapponese**, **Coreano**, **Hindi**, **Urdu** e **Persiano**.
* **Opzioni orologio**: Formato 12h / 24h, secondi in tempo reale opzionali e 3 dimensioni (Compatto, Medio, Grande).
* **Formati data USA e internazionali**: USA numerico (`MM/dd/yyyy`), USA esteso (`MMMM d, yyyy`), USA completo (`EEEE, MMMM d, yyyy`), USA compatto (`EEE, MMM d`), Internazionale (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Europeo (`dd.MM.yyyy`), Esteso o nascosta.
* **Stili di saluto**: Saluti poetici orari, calorosi, brevi, testo personalizzato o disattivato.

### 🎛️ Personalizzazione layout e home screen
* **Modalità icona barra superiore (Centro di controllo / Impostazioni dirette / Entrambi)**: Scegli se mostrare l'icona del Centro di controllo, saltare il Centro di controllo per aprire direttamente le Impostazioni, o mostrare entrambe le icone insieme sulla schermata Home.
* **Cursore predefinito su Impostazioni**: All'avvio dell'app il cursore è posizionato direttamente sull'icona delle Impostazioni per una configurazione rapida.
* **Spostamento verticale delle app verso il basso**: Possibilità di abbassare le righe delle app (6 livelli per nome, predefinito `450`) per dare risalto agli sfondi 4K.
* **Densità griglia**: Scelta tra 4, 5, 6 o 7 app per riga.
* **Opacità effetto vetro e Colori Dinamici Material You**: Regolazione della trasparenza delle schede da 10% a 100% e attivazione opzionale dei **Colori Dinamici Material You**.
* **Mostra tutte le app Android (Sideloaded APKs)**: Passa dalla visualizzazione delle sole app TV a **tutte le app Android installate** (incluse le APK mobili), con pulsanti rapidi per ripristinare le app nascoste o reimpostare l'ordine.

### 🎙️ Ricerca Vocale Universale, Assistente Vocale di Sistema e IPTV Interattivo
* **Ricerca Vocale e Istantanea (`🎤`)**: Cerca simultaneamente tra le app installate, i canali IPTV in diretta e sul Web/YouTube usando il microfono del telecomando.
* **Assistente vocale digitale predefinito di sistema**: Servizi integrati (`SalsaVoiceInteractionService` e `SalsaRecognitionService`) per impostare **Salsa TV** come assistente vocale predefinito del TV e usare nativamente il tasto microfono del telecomando.
* **Centro di Controllo Rapido (`Control Center`)**: Pannello rapido per cambiare formato 12h/24h, sfondo dinamico, avviare subito il salvaschermo, aprire la ricerca vocale o le impostazioni Wi-Fi.
* **Gestore Multi-Playlist IPTV e Barra Info Interattiva**: Aggiungi, cambia, aggiorna o elimina **più playlist M3U** e URL XMLTV EPG. Durante la visione IPTV, toccando lo schermo o premendo `OK` si apre la barra informazioni con **carosello canali orizzontale (`◀ / ▶`)**, guida EPG, timer di spegnimento, proporzioni schermo, blocco PIN per categoria e ripresa automatica dell'ultimo canale.
* **Avvio Pulito in Basso e Orologio Smart (240dp)**: Avvio con app e canali in basso (6 livelli per nome, predefinito `450`), nessun cursore selezionato all'avvio e riduzione automatica dell'orologio quando le icone raggiungono `240dp` dall'alto.
* **Segnalibri Web intelligenti (Icone HD e modalità YouTube TV)**: Estrazione automatica delle icone ad alta risoluzione per i siti web salvati, e apertura dei segnalibri YouTube direttamente nell'interfaccia **YouTube TV Leanback** ottimizzata per il telecomando.
* **Riga App Recenti e Aggiornamento OTA da GitHub**: Riga opzionale per le app aperte di recente e controllo/installazione degli aggiornamenti OTA da GitHub con un clic.

### 🔒 Privacy, Gestione Archiviazione, DNS Famiglia e Gestione App
* **Gestore archiviazione e app integrato**: Visualizza tutte le app installate ordinate per dimensione (`MB`), apri le informazioni di sistema o **disinstalla le app direttamente** per liberare spazio sul TV.
* **Griglia con 10 scorciatoie di sistema (con `Launcher Manager`)**: Accesso rapido a **Launcher Manager (HOME predefinito via ADB locale)**, **Opzioni sviluppatore**, **Wi-Fi**, **Ethernet**, **Schermo**, **Audio**, **Data e ora**, **Tutte le impostazioni**, **Info app** e **Gestione archiviazione**.
* **Scudo DNS a doppia modalità (AdBlock e Protezione Famiglia con PIN)**: Scegli tra **Blocco pubblicità e tracker** o **Modalità Protezione Famiglia** (blocca anche i siti per adulti su tutto il sistema). **Uscire dalla Protezione Famiglia o cambiare il PIN richiede tassativamente il PIN a 4 cifre.**
* **Blocco app con codice PIN a 4 cifre**: Protezione sicura per le applicazioni riservate e categorie IPTV.
* **Gestione app**: Sposta, classifica (App vs Giochi) o nascondi le app di sistema non necessarie.
* **Pulsante Indietro sicuro**: Previene l'uscita involontaria alla schermata di sistema del televisore.

> ⚠️ **Nota importante sulla Modalità Bambini / Protezione Famiglia**: L'uscita nel launcher è protetta da PIN. Tuttavia, **un launcher da solo non può impedire a un bambino di uscire tramite un'altra app o il tasto `Home`**, a meno che **Salsa TV Launcher** non sia impostato come **Launcher predefinito** e le **Impostazioni di sistema** siano bloccate con PIN.

---

## 🛡️ Sicurezza e Trasparenza dei Permessi

* **HTTPS predefinito (`network_security_config.xml`)**: Il traffico HTTP non crittografato è disattivato globalmente (`cleartextTrafficPermitted="false"`), con eccezioni mirate solo dove necessario.
* **Navigazione Sicura WebView attiva**: Google Safe Browsing abilitato (`EnableSafeBrowsing=true`) con telemetria disattivata (`MetricsOptOut=true`).
* **Aggiornamenti OTA Verificati**: Prima di installare aggiornamenti da GitHub, `AppUpdateManager` verifica l'hash **SHA-256** (`version.json`), il **nome del pacchetto** e il **certificato di firma APK**.
* **Nessun permesso di archiviazione esteso (`No MANAGE_EXTERNAL_STORAGE`)**: Sfondi, video locali, MP3, foto e backup utilizzano il selettore sicuro **Storage Access Framework (SAF)**.
* **Motivo dei permessi**: `INTERNET`/`ACCESS_NETWORK_STATE` (IPTV, pannelli, store, OTA), `ACCESS_COARSE_LOCATION` opzionale (meteo cittadino approssimativo), `RECORD_AUDIO` opzionale (solo per Ricerca Vocale `🎤`), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES` (solo su azione esplicita dell'utente), `BIND_VPN_SERVICE` (scudo locale AdGuard DNS) e `RECEIVE_BOOT_COMPLETED`.

---

## 📥 Download e installazione APK

> ℹ️ **Nota sulla Piattaforma**: Salsa TV Launcher è progettato principalmente per **Android TV, Google TV e TV Box** con telecomando (`D-Pad`). *(Il supporto per piccoli schermi mobile/touch è attualmente **sperimentale / in fase di sviluppo**).*

### Opzione 1: Download diretto APK
Scarica il file APK ufficiale pronto all'uso direttamente sul tuo dispositivo Android TV:

👉 **[Scarica l'ultimo APK di Salsa TV Launcher](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Codice breve per app Downloader: aftv.news/5157967](http://aftv.news/5157967)**

### Opzione 2: Installazione via ADB
```bash
adb connect <IP_DELLA_TV>:5555
adb install -r salsa-tv-launcher.apk
```

---

## 🎮 Scorciatoie telecomando

| Tasto | Azione |
| :--- | :--- |
| **Frecce direzionali (D-Pad)** | Navigazione rapida e fluida tra applicazioni, canali e impostazioni. |
| **Tasto OK / Centro** | Avvia l'applicazione o riproduce il canale selezionato. |
| **Pressione prolungata OK** | Apre le opzioni dell'app/canale (Sposta, Blocca con PIN, Nascondi, Giochi, Info/Disinstalla). |
| **Tasti Numerici (`0–9`)** | **Cambio Rapido Canale**: digita il numero del canale dalla Home, dalla lista IPTV o dal player per passare subito a quel canale (`OK` per confermare, `Indietro` per modificare). |
| **Tasto Menu (`☰` Tre linee)** | Contestuale: apre le **Impostazioni** nella Home, apre le **Impostazioni IPTV** in IPTV (`INFO`/`GUIDE` mostrano la barra info del player) e attiva/disattiva il **Mouse Virtuale** nei portali web e giochi. |
| **Tasto Voce / Google Assistant (`🎤`)** | Apre la ricerca vocale integrata per trovare app o canali IPTV. |
| **Tasto Indietro** | Torna alla schermata precedente o alla home in sicurezza. |

---

## 📄 Licenza e copyright

Progettato e sviluppato da **© Osama Salsa**.  
Questo repository ospita l'APK ufficiale compilato, la documentazione multilingue, gli screenshot e i manifesti di aggiornamento OTA verificati (`version.json`).

<div align="center">
  <sub>Creato con ❤️ per un'esperienza Android TV veloce, pulita e senza pubblicità in tutto il mondo.</sub>
</div>
