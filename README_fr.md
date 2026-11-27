[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | **Français** | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.0

![Écran d'accueil](docs/screenshots/home.png)

**Un lanceur et centre multimédia moderne, ultra-rapide, léger, hautement personnalisable et 100% sans publicité pour Android TV développé avec Jetpack Compose.**  
*La solution tout-en-un qui remplace des dizaines d'applications séparées sur votre téléviseur.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La solution tout-en-un

La plupart des lanceurs Android TV actuels sont saturés de publicités imposées, de bandes-annonces lourdes, de suggestions indésirables et d'interfaces lentes.

**Salsa TV Launcher** a été conçu pour offrir l'expérience inverse : un hub multimédia épuré, fluide, léger et totalement exempt de publicités ou de télémétrie.

### ✨ 7 modules intégrés fondamentaux
1. **🚀 Lanceur ultra-rapide et léger** : Démarrage instantané, décodage parallèle des icônes sur processeurs multi-cœurs et navigation 60 FPS sans ralentissement.
2. **📺 Lecteur IPTV avec favoris en accueil** : Prise en charge des listes M3U et lecture en 1 clic de vos chaînes préférées depuis l'écran principal.
3. **🎥 MiniTube** : Passerelle YouTube Leanback optimisée pour TV avec lecture fluide en mode invité.
4. **📥 Mini Downloader** : Gestionnaire de téléchargement rapide de fichiers et d'APK avec installateur automatique intelligent.
5. **🛒 Salsa Store** : Magasin web intégré pour découvrir et installer facilement des utilitaires Android TV.
6. **🕹️ 4 portails de jeux en ligne** : Des milliers de jeux HTML5 instantanés sans téléchargement (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Bloqueur de publicités à l'échelle du système** : Moteur AdGuard DNS intégré bloquant les fenêtres publicitaires et traceurs sur tout le téléviseur.

---

## 🌟 Nouvelles fonctionnalités et améliorations récentes

### 🆕 Nouveautés de la version 3.01
* **🔐 Système de PIN par Profil, Double Saisie Masquée, Verrouillage d'Accès & `Master PIN` Administrateur** : Suppression de tout code PIN par défaut (`1234` n'existe plus). Avant de verrouiller une application, une section ou une catégorie IPTV, l'utilisateur doit créer un code à 4 chiffres en **le saisissant deux fois pour confirmer**. Le PIN est toujours masqué (`••••`), sa modification ou suppression exige l'ancien PIN, chaque profil dispose d'une option **Verrouiller l'accès au profil par PIN**, et le profil **`Main` est l'Administrateur (`Admin`)** (non supprimable) dont le PIN sert de **`Master PIN Code`** pour accéder à n'importe quel profil ou le modifier en cas d'oubli.
* **🎙️ Recherche Vocale Android TV Plus Fiable** : Liaison directe avec le service vocal Android TV (`katniss`) avec tolérance étendue pour les micros de télécommande Bluetooth et nouvelle tentative automatique.
* **🏠 `Launcher Manager` Intégré (Configuration HOME par défaut via ADB local — sans PC ni téléphone)** : Définissez Salsa TV Launcher comme lanceur HOME permanent directement depuis l'application via ADB local (`127.0.0.1:5555`) avec clé RSA persistante, assistant en 3 étapes (`cmd package set-home-activity` avec repli `pm`), vérification en direct, restauration du lanceur d'origine en 1 clic et sélecteur système de secours (**Paramètres > Système et raccourcis TV > Launcher Manager**).
* **👤 Profils Multi-Utilisateurs, Couleur d'Accentuation Dynamique par Profil & Isolation (`Profiles / Users`)** : Créez plusieurs profils familiaux indépendants — chacun avec sa propre **Couleur d'Accentuation Dynamique du Thème** (8 nuances haute visibilité calibrées pour TV qui remplacent dynamiquement la couleur jaune/or sur le message d'accueil, les icônes de l'écran d'accueil [Applications, Jeux, Web, IPTV], les Paramètres, l'IPTV et tous les contours de focus), son propre **Thème & Fond d'écran**, **Économiseur d'écran**, **Langue**, **Favoris & Ordre IPTV**, **Ordre des Apps / Web / Jeux**, **code PIN optionnel (4 chiffres)** et **contrôle par section** (Afficher, Verrouiller par PIN ou Masquer **IPTV**, **Applications**, **Web** ou **Jeux**).
* **🔢 Zapping Numérique Rapide (`0–9`) & Badges de Chaînes Haute Lisibilité** : Tapez un numéro de chaîne sur la télécommande depuis l'Accueil, la liste IPTV ou le lecteur en direct pour y accéder instantanément, avec des badges numérotés à fort contraste (chiffres noirs sur fond blanc translucide).
* **🎞️ Économiseur Vidéo Local Appareil / USB (`Media3 / ExoPlayer`)** : En plus des flux plein écran propres via `m.youtube.com` sans aucune superposition, choisissez n'importe quelle vidéo locale sur votre TV ou clé USB via le sélecteur sécurisé sans permission (`SAF`) pour une lecture fluide en boucle plein écran.
* **📡 Économiseur de Texte Broadcast (12 Thèmes Animés, Fonds Sombres Dynamiques & Lecteur Vocal)** : 12 thèmes studio distincts — 6 classiques plus 6 nouveaux thèmes créatifs (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — avec animations, mises en page et **arrière-plans sombres dynamiques propres à chaque thème** aux ombres colorées contrastées + aperçus en direct dans les Paramètres, lignes personnalisables (`3–15s`), **Lecteur Vocal (TTS)** qui attend la fin de la lecture avant d'avancer, et musique d'ambiance ou MP3 local en option.
* **📺 Récupération Intelligente d'Erreur IPTV (`Chaîne suivante` + 15s) & Cadeau Secret 3× OK** : Lorsqu'une chaîne IPTV échoue, affiche un écran épuré avec une seule action (**Chaîne suivante**) et un **compte à rebours de 15 secondes** qui relance automatiquement la même chaîne. De plus, appuyer 3 fois sur `OK` sur la carte IPTV vide débloque les listes intégrées avec une animation festive de cadeau.
* **🛒 Catégories Prioritaires Salsa Store & Économiseur Vidéo Pur `m.youtube.com`** : **Salsa Store** place **Navigateurs Web** et **Réseaux Sociaux** en tête de son catalogue à 9 catégories d'utilitaires et lecteurs Android TV vérifiés (avec repli hors ligne) ; l'économiseur vidéo diffuse toujours proprement en plein écran via `m.youtube.com` sans aucun texte ni superposition sur l'image.
* **🎨 Réglages UI en Direct (`Live UI Settings`) — Format d'Heure Direct** : Le panneau flottant sur l'écran d'accueil intègre désormais un sélecteur direct de **Format d'Heure** (**`24h • 22:00`** ou **`12h • AM/PM`**) à côté de la taille d'horloge et du format de date, en plus de l'aperçu temps réel des fonds 4K, luminosité, décalage, grille, transparence, salutations, météo, filtre nocturne, Material You et mode léger.
* **🖥️ Économiseurs Tableaux de Bord, Lecture Vocale & Musique Unifiée Haute Clarté** : Affichez **Météo**, **Actualités**, **Économie**, **Football** ou **Rotation auto** ; le mode Actualités prend en charge la **lecture vocale (TTS)** des 6 titres avant de basculer, et tous les tableaux partagent les mêmes pistes musicales cristallines (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, sons d'ambiance et MP3) avec atténuation automatique pendant la voix.
* **🌙 Délai par Défaut de l'Économiseur à 10 Minutes** : Le délai d'inactivité par défaut passe à **10 minutes** (réglable : 2, 5, 10, 15, 30 min ou Désactivé).
* **🖱️ Souris Virtuelle Bord à Bord, Touche Menu Contextuelle (`☰`) & Version au Démarrage** : Le curseur virtuel dans les portails web et jeux atteint les bords haut et bas sans limite ; le bouton **Menu (`☰` Trois lignes)** ouvre les **Paramètres** sur l'Accueil, les **Paramètres IPTV** dans l'IPTV et bascule la **Souris Virtuelle** dans les portails web/jeux ; appui long fiable sur `OK` ; icône compacte **Salsa Store** ; mention `v3.0` et **mises à jour OTA via GitHub**.
* **🌐 Centre à 4 Tableaux en Direct, Portails Web & Moteur v3.0 en 21 Langues** : Accès rapide à **Météo**, **Actualités** (21 langues, 5 rubriques), **Économie & Marchés** et **Football (365Scores)**, portails web par défaut (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), barre supérieure adaptative et moteur v3.0 60 FPS.

### ⚡ Performances extrêmes et réactivité immédiate
* **Préchargement parallèle des icônes et bannières** : Toutes les applications et leurs bannières sont décodées simultanément en arrière-plan dès le lancement, s'affichant immédiatement à l'écran.
* **Navigation D-Pad ultra-rapide (110 ms)** : Accélération matérielle complète via `graphicsLayer` avec des transitions instantanées à 110 ms pour éliminer toute latence de la télécommande.
* **Effet de halo ambiant optimisé GPU** : Remplacement des flous logiciels lourds par des dégradés radiaux matériels fluides, garantissant 60 FPS stables sur tous les boîtiers TV.
* **Écran d'accueil épuré (Bouton Edit supprimé)** : La barre inférieure encombrante a été retirée pour laisser place aux applications, avec réorganisation rapide par **appui long sur `OK`**.

### 🌙 Écran de veille intelligent et relais du système
* **Passerelle intelligente vers l'écran de veille externe (`Désactivé`)** : La désactivation de la veille interne libère automatiquement le verrouillage d'écran (`FLAG_KEEP_SCREEN_ON`), permettant au veille système ou externe de fonctionner normalement.
* **Écran de veille immersif intégré (`Activé`)** : En sélectionnant un délai d'inactivité (2, 5, 10, 15 ou 30 min), le lanceur maintient l'écran allumé et propose trois styles :
  * **Horloge et date** : Heure lumineuse avec protection anti-brûlure de dalle.
  * **Fond 4K avec infos en coin** : Dégradé à 45° mettant en valeur l'heure, la date et le message d'accueil.
  * **Vidéo cinématique plein écran via `m.youtube.com` & flux personnalisés** : Ambiance feu de cheminée, chalet d'hiver ou cascades diffusée proprement en plein écran via `m.youtube.com` sans aucun texte ni superposition, avec possibilité d'**ajouter vos propres vidéos YouTube**, de restaurer les présélections et de lancer un **aperçu immédiat** depuis les Paramètres.
  * **Diaporama photo (`Slideshow`)** : Affichez les photos d'un dossier local/USB, des photos sélectionnées ou des galeries 4K avec durée réglable (`5s` à `5m`), mouvement **Ken Burns / Zoom doux / Statique**, 6 transitions d'entrée, effets atmosphériques (**Neige ❄️**, **Lucioles ✨**, **Pluie 🌧️**, **Feuilles d'automne 🍂**, **Sakura 🌸**, **Étoiles ⭐**, **Bokeh 🔮**), horloge en coin et pistes musicales limpides (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**) ou MP3 personnalisé.

### 🕒 Personnalisation complète de l'horloge, date et salutation
* **Typographie multilingue (21 langues)** : Traduction intégrale en **Français**, **Arabe (Police Cairo)**, **Anglais**, **Allemand**, **Espagnol**, **Italien**, **Russe**, **Portugais**, **Turc**, **Polonais**, **Néerlandais**, **Grec**, **Indonésien**, **Vietnamien**, **Thaï**, **Chinois**, **Japonais**, **Coréen**, **Hindi**, **Ourdou** et **Persan**.
* **Options d'horloge** : Format 12h / 24h, affichage optionnel des secondes et 3 tailles réglables (Compact, Moyen, Grand).
* **Formats de date américains et internationaux** : US numérique (`MM/dd/yyyy`), US long (`MMMM d, yyyy`), US complet (`EEEE, MMMM d, yyyy`), US compact (`EEE, MMM d`), International (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Européen (`dd.MM.yyyy`), Complet ou Masqué.
* **Salutations horaires** : Messages d'accueil poétiques, chaleureux, brefs, personnalisés ou masqués.

### 🎛️ Personnalisation de l'affichage d'accueil
* **Icône de la barre supérieure (Centre de contrôle / Paramètres directs / Les deux)** : Choisissez d'afficher le Centre de contrôle rapide, d'aller directement aux Paramètres en ignorant le Centre de contrôle, ou d'afficher les deux icônes ensemble sur l'écran d'accueil.
* **Curseur par défaut sur les paramètres** : Dès le lancement, la télécommande se positionne sur l'icône Paramètres pour une configuration rapide.
* **Décalage vertical des applications vers le bas** : Possibilité d'abaisser les rangées d'applications (6 niveaux nommés, `450` par défaut) pour admirer les fonds d'écran 4K.
* **Densité de la grille** : Choix entre 4, 5, 6 ou 7 applications par rangée.
* **Opacité de l'effet verre & couleurs dynamiques Material You** : Ajustement de la transparence des tuiles de 10% à 100% et activation optionnelle des **couleurs dynamiques Material You**.
* **Affichage des applications mobiles (Sideloaded APKs)** : Basculez entre l'affichage des applications TV uniquement ou de **toutes les applications Android installées** (y compris les APK mobiles), avec restauration des applications masquées ou réinitialisation de l'ordre en 1 clic.

### 🎙️ Recherche Vocale Universelle, Assistant Vocal Système & IPTV Interactif
* **Recherche Vocale et Instantanée (`🎤`)** : Recherchez simultanément parmi vos applications TV, vos chaînes IPTV en direct et sur le Web/YouTube via le microphone de la télécommande.
* **Assistant vocal numérique par défaut du système** : Services système intégrés (`SalsaVoiceInteractionService` et `SalsaRecognitionService`) permettant de définir **Salsa TV** comme assistant vocal par défaut du téléviseur pour utiliser le bouton micro de la télécommande nativement.
* **Centre de Contrôle Rapide (`Control Center`)** : Panneau d'accès rapide pour basculer l'horloge 12h/24h, le fond dynamique, lancer l'écran de veille immédiatement, ouvrir la recherche vocale ou accéder au Wi-Fi et aux Paramètres.
* **Gestionnaire multi-listes IPTV & Barre d'Infos Interactive** : Ajoutez, permutez, actualisez ou supprimez **plusieurs listes M3U** et URL EPG XMLTV. En regardant une chaîne IPTV, toucher l'écran ou appuyer sur `OK` affiche la barre d'informations avec un **carrousel horizontal de chaînes (`◀ / ▶`)**, guide EPG, minuterie de veille, format d'image, verrouillage PIN par catégorie et reprise automatique de la dernière chaîne.
* **Démarrage Épuré en Bas d'Écran & Horloge Intelligente (240dp)** : Démarre avec les applications et chaînes en bas (6 niveaux nommés, `450` par défaut), sans curseur visible au lancement, et l'horloge se réduit dès que les icônes atteignent `240dp` du haut.
* **Favoris Web intelligents (Icônes HD & mode YouTube TV)** : Extraction automatique des icônes haute résolution des sites ajoutés, et ouverture des favoris YouTube directement dans l'interface **YouTube TV Leanback** adaptée à la télécommande.
* **Barre d'applications récentes & mise à jour OTA GitHub** : Rangée optionnelle des applications récentes et vérification/installation des mises à jour OTA depuis GitHub en 1 clic.

### 🔒 Sécurité, Gestionnaire de Stockage, DNS Familial et Organisation
* **Gestionnaire de stockage et d'applications intégré** : Consultez toutes les applications installées triées par taille (`Mo`), ouvrez les informations système ou **désinstallez des applications directement** pour libérer de l'espace.
* **Grille de 10 raccourcis système rapides (avec `Launcher Manager`)** : Accès direct à **Launcher Manager (Accueil par défaut via ADB local)**, **Options développeurs**, **Wi-Fi**, **Ethernet**, **Affichage**, **Son**, **Date et heure**, **Paramètres système**, **Infos de l'application** et **Gestionnaire de stockage**.
* **Bouclier DNS double mode (AdBlock & Protection Familiale avec PIN)** : Choisissez entre le **blocage des publicités et traqueurs** ou le **mode Protection Familiale** (bloque également les sites pour adultes sur tout le système). **Quitter la Protection Familiale ou modifier le PIN requiert strictement le code PIN à 4 chiffres.**
* **Verrouillage par code PIN** : Sécurisation des applications privées et catégories IPTV avec un code à 4 chiffres.
* **Gestion des applications** : Déplacement, classement (Applications vs Jeux) ou masquage des applications système.
* **Sécurité du bouton Retour** : Empêche la fermeture accidentelle du lanceur vers le bureau Android.

> ⚠️ **Note importante sur le Mode Enfant / Protection Familiale** : La désactivation dans le lanceur est protégée par PIN. Toutefois, **un lanceur seul ne peut empêcher un enfant de quitter via une autre application ou la touche `Accueil`**, sauf si **Salsa TV Launcher** est défini comme **Lanceur par défaut** et que les **Paramètres système** sont également verrouillés par PIN.

---

## 🛡️ Sécurité & Transparence des Autorisations

* **HTTPS par défaut (`network_security_config.xml`)** : Le trafic HTTP non chiffré est désactivé globalement (`cleartextTrafficPermitted="false"`), avec des exceptions ciblées uniquement si nécessaire.
* **Navigation sécurisée WebView activée** : Google Safe Browsing est activé (`EnableSafeBrowsing=true`) sans collecte de télémétrie (`MetricsOptOut=true`).
* **Mises à jour OTA vérifiées** : Avant d'installer une mise à jour GitHub, `AppUpdateManager` vérifie l'empreinte **SHA-256** (`version.json`), le **nom du paquet** et le **certificat de signature APK**.
* **Aucune permission de stockage étendue (`Pas de MANAGE_EXTERNAL_STORAGE`)** : Tous les fonds, vidéos locales, MP3, photos et sauvegardes utilisent le sélecteur sécurisé **Storage Access Framework (SAF)**.
* **Détail des autorisations** : `INTERNET`/`ACCESS_NETWORK_STATE` (IPTV, tableaux de bord, Store, OTA), `ACCESS_COARSE_LOCATION` facultatif (météo approximative de la ville), `RECORD_AUDIO` facultatif (uniquement pour la Recherche Vocale `🎤`), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES` (uniquement sur action explicite de l'utilisateur), `BIND_VPN_SERVICE` (bouclier local AdGuard DNS) et `RECEIVE_BOOT_COMPLETED`.

---

## 📥 Téléchargement et installation

> ℹ️ **Note de plateforme** : Salsa TV Launcher est conçu principalement pour **Android TV, Google TV et boîtiers TV** pilotés à la télécommande (`D-Pad`). *(La prise en charge des petits écrans mobiles/tactiles est actuellement **expérimentale / en cours de développement**).*

### Option 1 : Téléchargement direct APK
Téléchargez directement l'APK officiel prêt à l'emploi sur votre Android TV :

👉 **[Télécharger le dernier APK d'Salsa TV Launcher](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Code court Downloader : aftv.news/5157967](http://aftv.news/5157967)**

### Option 2 : Installation via ADB
```bash
adb connect <IP_DE_VOTRE_TV>:5555
adb install -r salsa-tv-launcher.apk
```

---

## 🎮 Raccourcis télécommande

| Touche | Action |
| :--- | :--- |
| **Flèches directionnelles** | Navigation rapide et fluide entre applications et chaînes. |
| **Bouton OK / Centre** | Lance l'application ou la chaîne sélectionnée. |
| **Appui long OK** | Ouvre les options d'application/chaîne (Réorganiser, Verrouiller par PIN, Masquer, Jeux, Infos/Désinstaller). |
| **Touches Numériques (`0–9`)** | **Zapping Rapide** : saisissez un numéro de chaîne sur l'Accueil, dans la liste IPTV ou le lecteur pour y accéder directement (`OK` pour valider, `Retour` pour corriger). |
| **Touche Menu (`☰` Trois lignes)** | Contextuelle : ouvre les **Paramètres** sur l'Accueil, les **Paramètres IPTV** dans l'IPTV (`INFO`/`GUIDE` affichent la barre d'infos du lecteur) et bascule le mode **Souris Virtuelle** dans les portails web et jeux. |
| **Bouton Voix / Google Assistant (`🎤`)** | Ouvre la recherche vocale intégrée pour trouver des applications ou des chaînes IPTV. |
| **Bouton Retour** | Retourne à l'écran précédent ou à l'accueil en toute sécurité. |

---

## 📄 Licence et droits d'auteur

Conçu et développé par **© Osama Salsa**.  
Ce dépôt héberge l'APK officiel compilé, la documentation multilingue, les captures d'écran et les manifestes de mise à jour OTA vérifiés (`version.json`).

<div align="center">
  <sub>Fait avec ❤️ pour une expérience Android TV rapide, épurée et sans publicité dans le monde entier.</sub>
</div>
