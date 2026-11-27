[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | **Español** | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.0

![Pantalla Principal](docs/screenshots/home.png)

**Un lanzador y centro multimedia moderno, ultrarrápido, ligero, personalizable y 100% sin publicidad para Android TV creado con Jetpack Compose.**  
*La solución todo en uno que reemplaza docenas de aplicaciones independientes en tu televisor.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 La solución Todo en Uno

La mayoría de los lanzadores de Smart TV actuales están llenos de anuncios forzados, avances pesados de video y menús lentos.

**Salsa TV Launcher** fue diseñado para ser todo lo contrario: un centro multimedia limpio, ligero, veloz y totalmente libre de publicidad y rastreo.

### ✨ 7 módulos esenciales integrados
1. **🚀 Lanzador ultrarrápido y ligero**: Arranque inmediato, precarga paralela de iconos en procesadores multinúcleo y navegación fluida a 60 FPS sin tirones.
2. **📺 Reproductor IPTV con favoritos en inicio**: Compatible con listas M3U y reproducción con 1 clic de tus canales preferidos.
3. **🎥 MiniTube**: Portal de YouTube Leanback optimizado para TV con reproducción fluida en modo invitado.
4. **📥 Mini Downloader**: Descargador de alta velocidad de archivos y paquetes APK con autoinstalador inteligente.
5. **🛒 Salsa Store**: Tienda web integrada para descubrir e instalar aplicaciones y herramientas de Android TV con facilidad.
6. **🕹️ 4 portales de juegos online**: Miles de juegos HTML5 instantáneos sin descargas (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ Bloqueador de anuncios global**: Motor AdGuard DNS integrado que protege todo el televisor de banners y ventanas emergentes.

---

## 🌟 Novedades y últimas mejoras destacadas

### 🆕 Novedades en la Versión 3.1
* **🛒 Catálogo `Salsa Store` 100% en Directo (`https://auratv.freedev.app/index.html`) y Categorías Nativas de TV**: Ningún nombre de aplicación se almacena dentro de la app — cada vez que abres **Salsa Store**, carga los nombres e información en vivo desde `https://auratv.freedev.app/index.html` manteniendo el diseño nativo por categorías, con **Navegadores web** en primer lugar, **Redes sociales** en segundo lugar y luego el resto de categorías.
* **⌨️ Teclado en Pantalla Automático en Campos de Texto y Búsqueda Web (Excepto YouTube)**: Al hacer clic en cualquier cuadro de búsqueda o campo de texto dentro de páginas web se abre automáticamente el teclado predeterminado para escribir, excepto en **YouTube**, que utiliza su propio teclado integrado.
* **🔐 Sistema de PIN por Perfil, Confirmación Doble Oculta, Bloqueo de Entrada y `Master PIN` de Administrador**: Sin PIN predeterminado (`1234` eliminado). Antes de bloquear cualquier app, sección o categoría IPTV se debe crear un PIN de 4 dígitos **introduciéndolo dos veces para confirmar**. El PIN nunca se muestra en texto claro (`••••`), cambiarlo o eliminarlo exige el PIN actual, cada perfil cuenta con **Bloqueo de entrada con PIN**, y el perfil **`Main` es el Administrador (`Admin`)** (no eliminable) cuyo PIN actúa como **`Master PIN Code`** para abrir o editar cualquier perfil si se olvida su código.
* **🎙️ Mayor Fiabilidad en la Búsqueda por Voz en Android TV**: Vinculación directa con el servicio de voz de Android TV (`katniss`) con mayor tolerancia para micrófonos Bluetooth y reintento automático instantáneo.
* **🏠 `Launcher Manager` Integrado (Inicio HOME predeterminado vía ADB local — sin PC ni móvil)** : Configura Salsa TV Launcher como tu lanzador HOME permanente directamente desde la aplicación mediante ADB local (`127.0.0.1:5555`) con clave RSA persistente, asistente de 3 pasos (`cmd package set-home-activity` con respaldo `pm`), verificación en tiempo real, restauración del lanzador original en 1 clic y selector del sistema como alternativa (**Ajustes > Sistema y accesos directos de TV > Launcher Manager**).
* **👤 Perfiles Multiusuario, Color de Acento Dinámico por Usuario e Isolación (`Profiles / Users`)**: Crea múltiples perfiles familiares independientes — cada uno con su propio **Color de Acento Dinámico del Tema** (8 tonos de alto contraste calibrados para TV que reemplazan dinámicamente el color amarillo/dorado en el saludo, iconos de inicio [Apps, Juegos, Web, IPTV], Ajustes, IPTV y bordes de enfoque), su propia **Apariencia y Fondo de Pantalla**, **Salvapantallas**, **Idioma**, **Favoritos y Orden IPTV**, **Orden de Apps / Web / Juegos**, **PIN de Perfil opcional (4 dígitos)** y **control por sección** (Mostrar, Bloquear con PIN u Ocultar **IPTV**, **Apps**, **Web** o **Juegos**).
* **🔢 Cambio Rápido de Canal por Números (`0–9`) e Insignias de Alto Contraste**: Pulsa las teclas numéricas (`0–9`) en tu mando desde Inicio, la lista IPTV o el reproductor en vivo para saltar al instante a un canal por su número, acompañado de insignias numéricas nítidas de alto contraste (dígitos negros sobre fondo blanco translúcido).
* **🎞️ Salvapantallas de Vídeo Local desde Dispositivo / USB (`Media3 / ExoPlayer`)**: Además de las transmisiones limpias en pantalla completa vía `m.youtube.com` sin superposiciones, selecciona cualquier archivo de vídeo local de tu TV o USB mediante el selector seguro sin permisos (`SAF`) para reproducción continua en bucle.
* **📡 Salvapantallas de Texto Informativo (12 Temas Animados, Fondos Oscuros Dinámicos y Lector de Voz)**: 12 temas estilo estudio — 6 clásicos más 6 nuevos temas creativos (**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) — con diseños, animaciones y **fondos oscuros dinámicos propios para cada tema** con sombras de colores contrastantes + vistas previas en vivo en Ajustes, líneas editables (`3–15s`), **Lector de Voz (TTS)** que espera a terminar la narración antes de avanzar, y música ambiental o MP3 local opcional.
* **📺 Recuperación Inteligente de Errores IPTV (`Siguiente Canal` + Reintento 15s) y Regalo Secreto 3× OK**: Cuando un canal IPTV no carga, muestra un aviso limpio con una sola acción enfocada en **Siguiente Canal** y una **cuenta regresiva de 15 segundos** que reintenta automáticamente el mismo canal. Además, pulsar `OK` 3 veces en la tarjeta vacía de IPTV desbloquea las listas integradas con una animación festiva de regalo.
* **🛒 Categorías Top en Salsa Store y Salvapantallas de Vídeo Puro `m.youtube.com`**: **Salsa Store** prioriza **Navegadores Web** y **Redes Sociales** al principio de su catálogo de 9 categorías de utilidades y reproductores para Android TV (con respaldo offline); el salvapantallas de vídeo reproduce siempre en pantalla completa vía `m.youtube.com` sin texto ni superposiciones sobre el vídeo.
* **🎨 Ajustes UI en Vivo (`Live UI Settings`) — Selector Directo de Formato de Hora**: La barra flotante sobre el inicio añade ahora un control directo de **Formato de Hora** (**`24h • 22:00`** vs **`12h • AM/PM`**) junto al tamaño del reloj y formato de fecha, además de vista previa instantánea para fondos 4K, brillo, posición de apps, cuadrícula, opacidad, saludos, clima, filtro nocturno, Material You y modo ligero.
* **🖥️ Salvapantallas de Paneles, Lector de Voz y Música Unificada de Alta Claridad**: Configura **Clima**, **Noticias**, **Economía**, **Fútbol** o **Rotar Todo**; el modo Noticias admite **Lector de Voz (TTS)** que lee las 6 noticias antes de cambiar de panel, y todos los paneles comparten las mismas pistas musicales nítidas (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, ambientes y MP3) con atenuación automática al hablar TTS.
* **🌙 Tiempo Predeterminado del Salvapantallas de 10 Minutos**: El tiempo de inactividad predeterminado ahora es de **10 minutos** (configurable: 2, 5, 10, 15, 30 min o Desactivado).
* **🖱️ Ratón Virtual de Borde a Borde, Tecla Menú Contextual (`☰`) y Versión en Inicio**: El cursor virtual en portales web y juegos HTML5 alcanza los bordes superior e inferior por completo; el botón **Menú (`☰` Tres líneas)** abre **Ajustes** en Inicio, **Ajustes IPTV** en IPTV y alterna el **Ratón Virtual** en portales web/juegos; pulsación prolongada fiable en `OK`; icono compacto de **Salsa Store**; indicador `v3.0` y **actualizaciones automáticas OTA desde GitHub**.
* **🌐 Centro de 4 Paneles en Vivo, Portales Web y Motor v3.0 en 21 Idiomas**: Acceso directo a **Clima**, **Noticias** (21 idiomas, 5 secciones), **Economía y Mercados** y **Fútbol (365Scores)**, portales predeterminados (**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), barra superior sin recortes y motor v3.0 ligero a 60 FPS.

### ⚡ Rendimiento extremo y respuesta inmediata
* **Aparición instantánea de iconos y banners**: Todo el catálogo de apps se procesa en segundo plano en paralelo a través de los núcleos de la CPU, mostrándose de inmediato sin congelamientos.
* **Navegación con D-Pad ultrarrápida (110 ms)**: Aceleración por hardware completa mediante `graphicsLayer` con transiciones de 110 ms para eliminar cualquier latencia en el control remoto.
* **Efecto de halo ambiental optimizado para GPU**: Se reemplazaron los filtros de desenfoque pesados por suaves gradientes radiales por hardware, logrando 60 FPS estables.
* **Pantalla de inicio limpia (Botón Edit inferior eliminado)**: Se suprimió la barra inferior innecesaria para mayor claridad visual, manteniendo la reorganización mediante **pulsación prolongada en `OK`**.

### 🌙 Salvapantallas inteligente y cesión al sistema
* **Paso inteligente al salvapantallas externo (`Desactivado`)**: Al desactivar el salvapantallas interno, se libera automáticamente el bloqueo de pantalla (`FLAG_KEEP_SCREEN_ON`), permitiendo que el salvapantallas del sistema o una app externa funcione normalmente.
* **Salvapantallas integrado inteligente (`Activado`)**: Al seleccionar un tiempo de reposo (2, 5, 10, 15 o 30 min), el televisor se mantiene activo y muestra el estilo seleccionado:
  * **Reloj y fecha**: Hora brillante central con protección contra quemado de panel.
  * **Fondo 4K con información en esquina**: Gradiente a 45° que resalta la hora, fecha y saludo de forma elegante.
  * **Vídeo cinemático a pantalla completa vía `m.youtube.com` y vídeos personalizados**: Chimeneas cálidas, cabañas nevadas o cascadas en bucle continuo y limpio vía `m.youtube.com` sin texto ni superposiciones en pantalla, con opción de **añadir tus propios vídeos de YouTube**, restaurar preajustes y lanzar una **vista previa instantánea** desde Ajustes.
  * **Presentación de fotos (`Slideshow`)**: Muestra fotos desde una carpeta de tu dispositivo/USB, fotos seleccionadas o galerías 4K con duración ajustable (`5s` a `5m`), movimiento **Ken Burns / Zoom suave / Estático**, 6 transiciones de entrada, efectos atmosféricos (**Nieve ❄️**, **Luciérnagas ✨**, **Lluvia 🌧️**, **Hojas de otoño 🍂**, **Sakura 🌸**, **Estrellas ⭐**, **Bokeh 🔮**), reloj en esquina y pistas musicales nítidas (**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**) o MP3 personalizado.

### 🕒 Personalización completa de reloj, fecha y saludo
* **Soporte multilingüe (21 idiomas)**: Traducción completa al **Español**, **Árabe (Fuente Cairo)**, **Inglés**, **Alemán**, **Francés**, **Italiano**, **Ruso**, **Portugués**, **Turco**, **Polaco**, **Neerlandés**, **Griego**, **Indonesio**, **Vietnamita**, **Tailandés**, **Chino**, **Japonés**, **Coreano**, **Hindi**, **Urdu** y **Persa**.
* **Opciones de reloj**: Formato 12h / 24h, visualización opcional de segundos y 3 tamaños (Compacto, Mediano, Grande).
* **Formatos de fecha de EE. UU. e internacionales**: Numérico de EE. UU. (`MM/dd/yyyy`), Largo de EE. UU. (`MMMM d, yyyy`), Completo de EE. UU. (`EEEE, MMMM d, yyyy`), Compacto (`EEE, MMM d`), Internacional (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Europeo (`dd.MM.yyyy`), Completo o modo oculto.
* **Estilos de saludo**: Saludos poéticos por hora, cálidos, breves, texto personalizado propio o desactivado.

### 🎛️ Personalización del diseño y pantalla de inicio
* **Modo de icono superior (Centro de control / Ajustes directos / Ambos)**: Elige mostrar el icono del Centro de control rápido, omitir el Centro de control e ir directamente a Ajustes, o mostrar ambos iconos juntos en la pantalla de inicio.
* **Foco predeterminado en Ajustes**: Al iniciar, el cursor se ubica directamente sobre el icono de Ajustes para una configuración ágil.
* **Desplazamiento vertical de aplicaciones hacia abajo**: Ajuste para bajar las filas de apps (6 niveles por nombre, predeterminado `450`) para lucir los fondos 4K y la iluminación.
* **Densidad de cuadrícula**: Elección entre 4, 5, 6 o 7 aplicaciones por fila.
* **Control de opacidad de cristal y colores dinámicos Material You**: Ajuste de transparencia de 10% a 100% y opción de activar **colores dinámicos Material You**.
* **Filtro de aplicaciones móviles instaladas (Sideloaded APKs)**: Alterna entre mostrar solo aplicaciones de TV o **todas las aplicaciones Android instaladas** (incluidas APKs de móvil), con botones para restaurar apps ocultas o restablecer el orden en 1 clic.

### 🎙️ Búsqueda por Voz Universal, Asistente de Voz del Sistema y Barra IPTV Interactiva
* **Búsqueda Universal por Voz y Texto (`🎤`)**: Busca simultáneamente en todas tus aplicaciones instaladas, canales IPTV en vivo y la web/YouTube usando el micrófono del control remoto.
* **Asistente de voz digital predeterminado del sistema**: Servicios integrados (`SalsaVoiceInteractionService` y `SalsaRecognitionService`) que permiten configurar **Salsa TV** como el asistente de voz predeterminado del televisor para usar el botón de micrófono del mando de forma nativa.
* **Centro de Control Rápido (`Control Center`)**: Panel rápido para alternar formato 12h/24h, fondo dinámico, activar el salvapantallas al instante, abrir la búsqueda por voz o ir a Wi-Fi y Ajustes.
* **Gestor Multi-Lista IPTV y Barra de Información Interactiva**: Añade, cambia, actualiza o elimina **múltiples listas M3U** y enlaces XMLTV EPG. Mientras ves IPTV, tocar la pantalla o pulsar `OK` abre la barra de información con **carrusel horizontal de canales (`◀ / ▶`)**, guía EPG, temporizador de apagado, relación de aspecto, bloqueo PIN por categoría y reanudación automática del último canal.
* **Inicio Limpio en la Parte Inferior y Reloj Inteligente (240dp)**: Inicia con las filas abajo (6 niveles por nombre, predeterminado `450`), sin cursor resaltado al arrancar, y el reloj grande se reduce automáticamente cuando los iconos llegan a `240dp` del borde superior.
* **Marcadores Web inteligentes (Iconos HD y modo YouTube TV)**: Extracción automática de iconos en alta resolución para tus sitios web, y apertura de marcadores de YouTube directamente en la interfaz **YouTube TV Leanback** optimizada para el mando.
* **Fila de aplicaciones recientes y actualizador OTA desde GitHub**: Barra opcional de aplicaciones recientes y comprobación/instalación de actualizaciones OTA en 1 clic.

### 🔒 Privacidad, Gestor de Almacenamiento, DNS Familiar y Organización
* **Gestor de almacenamiento y aplicaciones integrado**: Revisa todas las aplicaciones instaladas ordenadas por su tamaño (`MB`), abre la información del sistema o **desinstala aplicaciones directamente** para liberar espacio.
* **Cuadrícula de 10 accesos directos del sistema (con `Launcher Manager`)**: Acceso en 1 clic a **Launcher Manager (Inicio predeterminado por ADB local)**, **Opciones de desarrollador**, **Wi-Fi**, **Ethernet**, **Pantalla**, **Sonido**, **Fecha y hora**, **Ajustes del sistema**, **Información de la app** y **Gestor de almacenamiento**.
* **Escudo DNS de doble modo (AdBlock y Protección Familiar con PIN)**: Elige entre **Bloqueo de anuncios y rastreadores** o **Modo de Protección Familiar** (bloquea también sitios para adultos en todo el sistema). **Salir del Modo de Protección Familiar o cambiar el PIN requiere estrictamente introducir el PIN de 4 dígitos.**
* **Bloqueo con PIN de 4 dígitos**: Protege aplicaciones privadas y categorías IPTV de accesos no autorizados.
* **Organización de apps**: Mueve, clasifica (Apps vs Juegos) u oculta aplicaciones de sistema no deseadas.
* **Botón Atrás seguro**: Evita salidas accidentales al escritorio de Android TV al pulsar la tecla Volver en la pantalla principal.

> ⚠️ **Nota importante sobre el Modo Infantil / Protección Familiar**: Salir del modo dentro del lanzador está protegido con PIN. No obstante, **un lanzador por sí solo no puede impedir que un niño salga usando otra app o el botón `Home`**, a menos que configures **Salsa TV Launcher** como **Lanzador predeterminado** y bloquees con PIN los **Ajustes del sistema** (y otros navegadores/lanzadores).

---

## 🛡️ Seguridad y Transparencia de Permisos

* **HTTPS por defecto (`network_security_config.xml`)**: El tráfico HTTP sin cifrar está deshabilitado globalmente (`cleartextTrafficPermitted="false"`), salvo excepciones acotadas donde sea necesario.
* **Navegación Segura de WebView activada**: Google Safe Browsing activo (`EnableSafeBrowsing=true`) con métricas desactivadas (`MetricsOptOut=true`).
* **Actualizaciones OTA Verificadas**: Antes de instalar cualquier actualización de GitHub, `AppUpdateManager` comprueba la firma **SHA-256** (`version.json`), el **nombre del paquete** y el **certificado de firma del APK**.
* **Sin permisos amplios de almacenamiento (`Sin MANAGE_EXTERNAL_STORAGE`)**: Todos los fondos, vídeos locales, MP3, fotos y copias usan el selector seguro sin permisos **Storage Access Framework (SAF)**.
* **Uso de permisos**: `INTERNET`/`ACCESS_NETWORK_STATE` (IPTV, paneles, tienda, OTA), `ACCESS_COARSE_LOCATION` opcional (clima aproximado de ciudad), `RECORD_AUDIO` opcional (solo al abrir Búsqueda por Voz `🎤`), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES` (solo cuando instalas/desinstalas manualmente), `BIND_VPN_SERVICE` (escudo local AdGuard DNS) y `RECEIVE_BOOT_COMPLETED`.

---

## 📥 Descarga e instalación de APK

> ℹ️ **Nota de Plataforma**: Salsa TV Launcher está diseñado principalmente para **Android TV, Google TV y TV Boxes** controlados con mando (`D-Pad`). *(El soporte para pantallas pequeñas táctiles/móviles es actualmente **experimental / en desarrollo**).*

### Opción 1: Descarga directa de APK
Descarga el archivo APK oficial directamente en tu dispositivo Android TV:

👉 **[Descargar la última versión de Salsa TV Launcher](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Código corto para app Downloader: aftv.news/5157967](http://aftv.news/5157967)**

### Opción 2: Instalación vía ADB
```bash
adb connect <IP_DE_TU_TV>:5555
adb install -r salsa-tv-launcher.apk
```

---

## 🎮 Atajos del control remoto

| Tecla | Acción |
| :--- | :--- |
| **Flechas del D-Pad** | Navegación de alta velocidad fluida entre apps, canales y ajustes. |
| **Botón OK / Centro** | Abre la aplicación o reproduce el canal seleccionado. |
| **Pulsación larga en OK** | Abre las opciones de app/canal (Mover, Bloquear con PIN, Ocultar, Juegos, Info/Desinstalar). |
| **Teclas Numéricas (`0–9`)** | **Cambio Rápido de Canal**: escribe el número de canal en Inicio, lista IPTV o reproductor para ir directamente a él (`OK` para confirmar, `Atrás` para corregir). |
| **Tecla Menú (`☰` Tres líneas)** | Sensible al contexto: abre **Ajustes** en la pantalla de inicio, abre **Ajustes IPTV** en IPTV (`INFO`/`GUIDE` alternan la barra de info del reproductor) y activa/desactiva el **Ratón Virtual** en portales web y juegos. |
| **Botón de Voz / Google Assistant (`🎤`)** | Abre la búsqueda por voz integrada para encontrar aplicaciones o canales IPTV. |
| **Botón Atrás (Back)** | Vuelve a la pantalla anterior o a la principal de forma segura. |

---

## 📄 Licencia y derechos de autor

Diseñado y desarrollado por **© Osama Salsa**.  
Este repositorio aloja el APK oficial compilado, la documentación multilingüe, las capturas de pantalla y los manifiestos de actualización OTA verificados (`version.json`).

<div align="center">
  <sub>Hecho con ❤️ para una experiencia Android TV veloz, limpia y sin anuncios en todo el mundo.</sub>
</div>
