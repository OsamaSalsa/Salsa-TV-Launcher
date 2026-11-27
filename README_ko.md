[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | [Tiếng Việt](README_vi.md) | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | **한국어** | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 📺 Salsa TV Launcher v3.0

![홈 화면](docs/screenshots/home.png)

**Jetpack Compose로 제작된 현대적이고 사용자 정의가 자유로우며 초고속이고 100% 광고가 없는 Android TV 런처 및 미디어 센터입니다.**  
*기기에 설치된 수십 개의 개별 앱을 하나로 대체하는 올인원 TV 솔루션입니다.*

[![Android](https://img.shields.io/badge/Platform-Android%20TV-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-3.0-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Clean & Fast](https://img.shields.io/badge/Performance-Blazing%20Fast%20%26%20Lightweight-FFD700.svg?style=for-the-badge&logo=speedtest)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Ad-Free](https://img.shields.io/badge/Experience-100%25%20Ad--Free-10B981.svg?style=for-the-badge&logo=adguard)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

---

</div>

## 🚀 올인원 TV 솔루션

오늘날 대부분의 스마트 TV 런처는 강제 광고, 무거운 동영상 미리보기, 원치 않는 추천 콘텐츠, 느린 UI로 가득 차 있습니다.

**Salsa TV Launcher**는 그 정반대를 목표로 설계되었습니다. 단순한 홈 화면을 넘어, 여러 TV 앱을 하나의 깔끔하고 빠르며 완전히 광고 없는 허브로 통합하는 올인원 미디어 센터입니다.

### ✨ 핵심 통합 모듈
1. **🚀 초고속 경량 런처**: 1초 미만의 빠른 앱 실행, GPU 가속 포커스 이동, 병렬 아이콘 사전 로딩, CPU 부하 0%.
2. **📺 내장 IPTV 플레이어 및 즐겨찾기 행**: M3U 재생목록을 지원하며 홈 화면에서 클릭 한 번으로 즐겨찾는 채널을 즉시 재생합니다.
3. **🎥 MiniTube**: 빠른 게스트 재생과 완전한 TV 리모컨 제어를 지원하는 가벼운 YouTube TV Leanback 경험.
4. **📥 Mini Downloader**: 스마트 원클릭 패키지 설치 기능을 갖춘 고속 파일 및 APK 다운로더.
5. **🛒 Salsa Store**: Android TV 도구와 유틸리티를 간편하게 탐색하고 설치할 수 있는 깔끔한 웹 앱 마켓.
6. **🕹️ 4가지 게임 포털**: 별도 다운로드 없이 즉시 즐기는 수천 개의 온라인 HTML5 게임 (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**).
7. **🛡️ 시스템 전체 광고 및 추적 차단기**: 내장 AdGuard DNS 보호 기능으로 TV 전체의 팝업, 배너 및 데이터 추적을 차단합니다.

---

## 🌟 새로운 기능 및 최신 개선 사항

### 🆕 버전 3.0 새로운 기능
* **🛠️ Launcher Manager (`설정 > 빠른 시스템 바로가기 > Launcher Manager` — 로컬 ADB로 기본 홈 런처 설정)**: 외부 PC나 스마트폰 없이 기기 자체의 로컬 ADB 연결(`127.0.0.1:5555`)을 통해 `cmd package set-home-activity --user 0` 명령을 실행하여 재부팅 후에도 유지되는 영구 기본 `HOME` 런처로 설정하는 내장 3단계 마법사를 제공합니다(순정 런처 비활성화 및 원클릭 복원 포함).
* **🎨 사용자별 다이내믹 테마 강조 색상**: 각 프로필에서 선택한 테마 강조 색상이 기존의 노란색/골드 색상을 대신하여 환영 인사말, 텍스트 강조, 앱/게임/웹 섹션 헤더 및 아이콘, 설정, IPTV, Salsa Store, 포커스 테두리 등 앱 전체에 동적으로 적용됩니다.
* **👤 멀티 유저 프로필 및 사용자별 독립 설정 (`Profiles / Users`)**: 가족 구성원별로 독립된 프로필을 여러 개 생성할 수 있으며, 각 프로필마다 **테마 강조 색상 및 배경화면**, **화면보호기**, **언어**, **IPTV 즐겨찾기 및 채널 순서**, **앱/웹/게임 순서**, 선택형 **4자리 프로필 PIN** 및 **섹션별 제어**(**IPTV**, **앱**, **웹**, **게임** 표시 / PIN 잠금 / 숨기기)가 개별 저장됩니다. 상단 바 가장 바깥쪽(**LTR 기준 우측 끝, RTL 기준 좌측 끝**)의 테두리 없는 플랫 원형 아바타 아이콘으로 빠르게 전환할 수 있습니다.
* **🔢 숫자 키 빠른 채널 전환 (`0–9`) 및 고대비 채널 번호 배지**: 홈 화면, IPTV 채널 목록 또는 실시간 플레이어에서 리모컨 숫자 키(`0–9`)를 입력해 원하는 채널로 즉시 이동하며, 선명한 고대비 채널 번호 배지(반투명 흰색 바탕에 검은색 숫자)를 제공합니다.
* **🎞️ 기기 / USB 로컬 동영상 화면보호기 (`Media3 / ExoPlayer`)**: 텍스트나 오버레이 없는 깔끔한 `m.youtube.com` 전체 화면 스트림 외에도, 별도 권한 없는 안전한 **Storage Access Framework(SAF)**로 TV 저장소나 USB의 로컬 동영상을 선택해 전체 화면으로 반복 재생할 수 있습니다.
* **📡 방송 텍스트 화면보호기 (12가지 애니메이션 테마, 어두운 동적 배경 & 음성 읽기)**: 6가지 클래식 테마 + 6가지 창의적인 신작 테마(**Aurora Silk**, **Synthwave 80s**, **Cosmic Orbit**, **Royal Arabesque**, **Audio Studio Equalizer**, **Crystal Prism Hologram**) 등 총 12종의 스튜디오 테마 제공. 각기 다른 모션·레이아웃과 대비되는 컬러 그림자의 **테마별 전용 어두운 동적 배경** + 설정 내 실시간 미리보기, 문구 편집 및 표시 시간(`3~15초`), 낭독이 끝날 때까지 기다리는 **음성 읽기(TTS)**, 배경음/로컬 MP3 지원.
* **📺 IPTV 스마트 오류 복구 (`다음 채널` + 15초 자동 재시도) & 비밀 3× OK 선물 해제**: IPTV 채널 재생 실패 시 **다음 채널** 버튼 하나에만 포커스된 깔끔한 화면과 **15초 카운트다운**을 표시하며 자동 재시도합니다. 또한 빈 IPTV 카드에서 `OK`를 3번 누르면 축하 선물 상자 애니메이션과 함께 내장 재생목록이 잠금 해제됩니다.
* **🛒 Salsa Store 상위 카테고리 & 깔끔한 `m.youtube.com` 동영상 화면보호기**: **Salsa Store**의 9개 카테고리 카탈로그(엄선된 Android TV 유틸리티 및 플레이어, 오프라인 지원)에서 **웹 브라우저**와 **소셜 미디어**를 최상단에 배치했으며, 동영상 화면보호기는 화면 위 텍스트나 오버레이 없이 `m.youtube.com`으로 깔끔하게 전체 화면 재생됩니다.
* **🎨 실시간 UI 설정 HUD (`Live UI Settings`) — 시계 형식 바로 전환**: 홈 화면 투명 설정 바의 시계 크기와 날짜 형식 바로 옆에 **시계 형식(`24시간 • 22:00` / `12시간 • AM/PM`)** 전환을 직접 추가했으며 4K 배경, 밝기, 앱 위치, 그리드, 투명도, 인사말, 날씨, 야간 필터, Material You, 저사양 모드를 실시간 미리보기로 조절할 수 있습니다.
* **🖥️ 대시보드 화면보호기, 뉴스 음성 읽기 & 통합 선명 음악**: **날씨**, **뉴스**, **경제**, **축구** 또는 **전체 자동 순환**을 지원하며, 뉴스 모드는 6개 기사를 모두 읽어준 뒤 전환하는 **음성 읽기(TTS)**를 제공하고 모든 정보 대시보드가 슬라이드쇼/텍스트와 동일한 선명한 배경 음악(**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, 앰비언트, MP3) 및 음성 재생 시 자동 볼륨 감소를 공유합니다.
* **🌙 기본 화면보호기 대기 시간 10분으로 변경**: 기본 비활성 대기 시간을 **10분**으로 변경했습니다 (2, 5, 10, 15, 30분 또는 사용 안 함으로 설정 가능).
* **🖱️ 상하 끝까지 닿는 가상 마우스, 상황별 3선 메뉴(`☰`) & 시작 화면 버전 표시**: 웹 포털과 HTML5 게임에서 가상 마우스가 화면 최상단·최하단까지 이동하며, **메뉴 버튼(`☰` 세 줄)**은 홈 화면에서 **설정**, IPTV에서 **IPTV 설정**을 열고 웹 포털/게임에서는 **가상 마우스**를 전환합니다. `OK` 길게 누르기 오작동 방지, 새 **Salsa Store** 아이콘, `v3.0` 표시 및 **GitHub OTA 자동 업데이트** 지원.
* **🌐 4대 실시간 대시보드, 기본 웹 포털 & 21개 언어 v3.0 엔진**: **날씨**, **뉴스**(21개 언어 5개 섹션), **경제 및 시장**, **축구(365Scores)**, 기본 포털(**YouTube TV**, **Google**, **CrazyGames**, **Poki**, **Playhop**, **EasyGames**), 잘림 없는 상단 바 및 가볍고 빠른 60FPS v3.0 엔진.

### ⚡ 극강의 성능과 즉각적인 앱 실행
* **아이콘 및 배너 병렬 프리로딩**: 앱 패키지, 아이콘 및 배너를 백그라운드 멀티코어 CPU에서 동시에 디코딩하여 실행 시 멈춤 없이 즉시 표시합니다.
* **초고속 110ms D-Pad 포커스 탐색**: 하드웨어 가속 `graphicsLayer` 렌더링과 110ms의 민첩한 전환 효과로 리모컨 조작 지연을 완전히 제거했습니다.
* **GPU 최적화 다이내믹 앰비언트 글로우**: 무거운 블러 및 노이즈 레이어를 부드러운 하드웨어 방사형 그라데이션으로 대체해 어떤 TV에서도 60fps를 유지합니다.
* **깔끔한 홈 화면 (하단 편집 버튼 제거)**: 시선을 앱과 채널에 집중시키고, 리모컨 **`OK` 버튼을 길게 눌러** 빠르게 정렬할 수 있습니다.

### 🌙 지능형 화면 보호기 및 외부 화면 보호기 전환
* **스마트 외부 화면 보호기 전환 (`비활성화` 모드)**: 앱 내 화면 보호기를 끄면 화면 켜짐 유지 잠금(`FLAG_KEEP_SCREEN_ON`)이 자동으로 해제되어 Android TV 시스템이나 외부 화면 보호기가 정상 작동합니다.
* **내장 앰비언트 절전 모드 (`활성화`)**: 대기 시간(2, 5, 10, 15 또는 30분)을 설정하면 TV를 깨운 상태로 내장 화면 보호기를 실행합니다:
  * **시계 및 날짜 모드**: 번인 방지(드리프트 보호) 기능이 포함된 선명한 시계와 날짜.
  * **4K 배경화면 및 정보 모드**: 45도 모서리 그라데이션 위에 시계, 날짜 및 실시간 인사말 표시.
  * **시네마틱 비디오 화면 보호기 (`m.youtube.com`, 사용자 지정 YouTube 비디오 및 실시간 미리보기)**: 텍스트나 배너 오버레이 없이 `m.youtube.com`을 통해 전체 화면으로 벽난로, 겨울 오두막, 열대 폭포 영상을 감상하세요. **사용자 지정 YouTube URL/ID 추가**, 기본 비디오 삭제/복원 및 설정에서 **즉시 실시간 미리보기**를 지원합니다.
  * **사진 슬라이드쇼 화면 보호기 (`Slideshow`)**: 기기 저장소/USB의 특정 폴더, 선택한 사진 또는 4K 갤러리를 슬라이드쇼로 재생합니다. 사진별 표시 시간(`5초`~`5분`), 모션(**Ken Burns** / **부드러운 줌** / **정지**), 6가지 전환 효과, 7가지 분위기 효과(**눈 내림 ❄️**, **반딧불이 ✨**, **잔잔한 비 🌧️**, **단풍잎 🍂**, **벚꽃잎 🌸**, **반짝이는 별 ⭐**, **보케 조명 🔮**), 모서리 시계 및 선명한 배경 음악(**Bossa Nova**, **Smooth Jazz**, **Calm Ocean Waves**, **Classic Chillout**, 내장 사운드/MP3)을 지원합니다.

### 🕒 완벽한 시계, 날짜 및 인사말 맞춤 설정
* **21개 언어 다국어 타이포그래피**: **한국어**, **아랍어(Cairo 글꼴)**, **영어**, **독일어**, **프랑스어**, **이탈리아어**, **스페인어**, **러시아어**, **포르투갈어**, **튀르키예어**, **폴란드어**, **네덜란드어**, **그리스어**, **인도네시아어**, **베트남어**, **태국어**, **중국어**, **일본어**, **힌디어**, **우르두어**, **페르시아어**를 완벽 지원합니다.
* **시계 설정**: 12시간/24시간 전환, 실시간 초 표시 선택, 3가지 크기(작게, 중간, 크게).
* **미국식 및 국제 날짜 형식**: 미국 숫자(`MM/dd/yyyy`), 미국 긴 형식(`MMMM d, yyyy`), 미국 요일 포함(`EEEE, MMMM d, yyyy`), 미국 요약(`EEE, MMM d`), 국제(`dd/MM/yyyy`), ISO(`yyyy-MM-dd`), 유럽식(`dd.MM.yyyy`), 전체 날짜 또는 숨김.
* **인사말 스타일**: 시간대별 시적인 인사말, 따뜻한 인사말, 간단한 인사말, 사용자 지정 문구 또는 숨김.

### 🎛️ 런처 및 홈 화면 레이아웃 설정
* **상단 바 컨트롤 센터 및 설정 바로가기 모드**: 홈 화면 우측 상단에 **빠른 컨트롤 센터 아이콘**을 표시하거나, 컨트롤 센터를 건너뛰고 **설정 아이콘을 바로 표시**하거나, **두 아이콘을 함께 표시**할 수 있습니다.
* **설정 아이콘 기본 포커스**: 런처 부팅 시 커서가 오른쪽 상단 설정 아이콘에 맞춰져 빠르게 구성을 변경할 수 있습니다.
* **앱 행 상단 여백 조절**: 앱 행을 아래로 내려(180dp~380dp) 고해상도 4K 배경화면과 오로라 배경 효과를 더욱 돋보이게 합니다.
* **맞춤형 그리드 밀도**: 화면 크기와 시청 거리에 맞게 한 줄당 4, 5, 6 또는 7개의 앱을 선택하세요.
* **글래스모피즘 투명도 제어**: 반투명 카드 투명도를 10%에서 100%까지 조절할 수 있습니다.
* **사이드로드 / 모바일 앱 표시 전환 및 Material You 동적 색상**: Android TV 전용 앱만 표시하거나 **사이드로드된 모바일/태블릿 APK를 포함한 모든 설치된 앱**을 표시하도록 전환할 수 있으며, 숨긴 앱 복원, 정렬 초기화 및 **Material You 동적 색상**을 지원합니다.

### 🎙️ 통합 음성 검색, 시스템 기본 음성 비서, 제어 센터 및 IPTV 관리
* **통합 음성 및 즉시 검색 (`🎤`)**: 리모컨 마이크나 화상 키보드로 설치된 모든 TV 앱, 실시간 IPTV 채널, 웹/YouTube를 동시에 검색합니다.
* **시스템 기본 디지털 음성 비서 (`VoiceInteractionService`)**: Salsa TV를 Android TV의 **기본 디지털 음성 비서**로 설정하여 리모컨의 물리적 마이크 버튼으로 즉시 호출할 수 있습니다.
* **빠른 제어 센터 (`Control Center`)**: 12/24시간제 전환, 동적 배경, 화면 보호기 즉시 실행, 음성 검색, Wi-Fi 및 설정을 빠르게 제어합니다.
* **다중 플레이리스트 IPTV 관리자 및 스마트 웹 북마크**: 여러 **M3U 플레이리스트**와 **XMLTV EPG 주소**를 추가, 전환 및 새로고침할 수 있으며, 웹 북마크는 **고해상도 아이콘 자동 추출기(`WebBookmarkIconResolver`)**와 **YouTube TV Leanback 전체 화면 모드**를 지원합니다.
* **터치 및 리모컨 `OK` IPTV 정보 바 (채널 스트립 내장)**: IPTV 시청 중 화면을 터치하거나 리모컨의 `OK` 버튼을 누르면 **가로 채널 탐색 스트립**이 포함된 정보 바가 열려 바로 채널을 전환할 수 있습니다 (실시간 EPG, 취침 예약 지원).
* **깔끔한 하단 시작 및 240dp 스마트 시계 축소**: 시작 시 앱과 채널이 하단(6단계 이름 옵션, 기본값 `450`)에 위치하며 커서가 아무 곳에도 머물지 않고, 아이콘이 상단 `240dp`에 도달하면 큰 시계가 즉시 작아집니다.

### 🔒 개인정보 보호, 듀얼 모드 DNS 실드, 앱 저장소 관리자 및 앱 잠금
* **내장 앱 저장소 관리자**: 설치된 모든 앱을 용량(MB) 순으로 확인하고, 시스템 앱 정보를 열거나 불필요한 앱을 즉시 삭제하여 TV 저장 공간을 확보할 수 있습니다.
* **듀얼 모드 DNS 실드 (광고 차단 및 PIN 보호 가족 보호 모드)**: 루팅 없이 로컬 VPN 기반으로 동작하며 **광고 및 추적기 차단 모드** 또는 **가족 보호 모드**를 선택할 수 있습니다. **가족 보호 모드 해제 및 PIN 변경은 반드시 4자리 PIN 입력이 필요합니다.**
* **10가지 빠른 시스템 바로가기 그리드 (`Launcher Manager` 포함)**: 설정에서 **Wi-Fi, 이더넷, 디스플레이, 사운드, 날짜 및 시간, 전체 시스템 설정, 앱 정보, 저장소 관리자, Launcher Manager(로컬 ADB 기본 홈 설정), 개발자 옵션**으로 한 번에 이동할 수 있습니다.
* **PIN 보호 앱 잠금**: 4자리 PIN 코드로 민감한 앱을 안전하게 잠급니다.
* **앱 재정렬 및 숨기기**: 앱 이동, 분류(앱 / 게임) 또는 불필요한 기본 시스템 앱 숨기기.
* **안전한 뒤로 가기 탐색**: 홈 화면에서 뒤로 가기 버튼을 눌렀을 때 실수로 시스템 바탕화면으로 나가는 것을 방지합니다.

> ⚠️ **어린이 / 가족 보호 모드 주의사항**: 런처 내부에서의 해제는 4자리 PIN으로 보호됩니다. 다만 **런처 단독으로는 다른 설치된 앱이나 시스템 `Home` 버튼을 통한 이탈을 막을 수 없으므로**, **Salsa TV Launcher**를 **기본 홈 앱**으로 설정하고 **시스템 설정**도 앱 잠금 PIN으로 잠가 두시기 바랍니다.

---

## 🛡️ 보안 및 권한 투명성

* **기본 HTTPS 통신 (`network_security_config.xml`)**: 암호화되지 않은 HTTP 트래픽을 전역 차단(`cleartextTrafficPermitted="false"`)하고 필요한 최소 예외만 허용합니다.
* **WebView 세이프 브라우징 활성화**: 모든 내장 WebView에 Google Safe Browsing이 활성화(`EnableSafeBrowsing=true`)되며 원격 측정은 비활성화(`MetricsOptOut=true`)됩니다.
* **검증된 OTA 자동 업데이트**: GitHub에서 내려받은 업데이트 APK 설치 전 **SHA-256 해시**(`version.json`), **패키지 이름**, **APK 서명 인증서**를 검증합니다.
* **광범위한 저장소 권한 미사용 (`MANAGE_EXTERNAL_STORAGE` 없음)**: 모든 로컬 배경화면, 동영상, MP3, 사진 및 백업은 권한 없는 **Storage Access Framework(SAF)** 선택기를 사용합니다.
* **권한 사용 목적**: `INTERNET`/`ACCESS_NETWORK_STATE`(IPTV, 대시보드, 스토어, OTA), 선택형 `ACCESS_COARSE_LOCATION`(도시 수준 날씨), 선택형 `RECORD_AUDIO`(음성 검색 `🎤` 전용), `REQUEST_INSTALL_PACKAGES`/`REQUEST_DELETE_PACKAGES`(사용자 직접 설치/삭제 시), `BIND_VPN_SERVICE`(로컬 AdGuard DNS 보호), `RECEIVE_BOOT_COMPLETED`.

---

## 📱 스크린샷

<div align="center">

| 홈 화면 및 앰비언트 글로우 | 4K 배경화면 및 사용자 사진 |
| :---: | :---: |
| *![홈 화면](docs/screenshots/home_screen.png)* | *![배경화면](docs/screenshots/wallpaper_grid.png)* |

| 통합 IPTV 플레이어 | Salsa Store |
| :---: | :---: |
| *![IPTV 플레이어](docs/screenshots/iptv_player.png)* | *![Salsa Store](docs/screenshots/aura_store.png)* |

| 게임 포털 | Mini Downloader |
| :---: | :---: |
| *![게임 포털](docs/screenshots/gaming_portals.png)* | *![Mini Downloader](docs/screenshots/mini_downloader.png)* |

</div>

---

## 📥 APK 다운로드 및 설치

> ℹ️ **플랫폼 안내**: Salsa TV Launcher는 리모컨(`D-Pad`)으로 조작하는 **Android TV, Google TV 및 TV 박스**를 위해 설계되었습니다. *(소형 모바일/터치 화면 지원은 현재 **실험적 / 개발 중**입니다).*

### 방법 1: 직접 APK 다운로드
최신 공식 릴리스 APK 파일을 Android TV 기기로 직접 다운로드하세요:

👉 **[최신 Salsa TV Launcher APK 다운로드](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)**  
👉 **[Downloader 단축 코드 / 직링크: aftv.news/5157967](http://aftv.news/5157967)**

### 방법 2: ADB를 통한 설치
```bash
# Android TV IP 주소에 연결
adb connect <YOUR_TV_IP_ADDRESS>:5555

# APK 설치
adb install -r salsa-tv-launcher.apk
```

### 방법 3: 기본 홈 런처로 설정 (내장 로컬 ADB Launcher Manager)
1. Salsa TV Launcher 내의 **설정 > 빠른 시스템 바로가기 > Launcher Manager**를 엽니다.
2. 3단계 마법사에 따라 **개발자 옵션**과 **USB / 네트워크 디버깅**을 켜고 **연결 (127.0.0.1:5555)**을 누른 뒤 시스템 창에서 **항상 허용**을 선택합니다.
3. **기본 HOME으로 설정**을 누르면 외부 PC 없이 재부팅 후에도 유지되는 기본 홈 런처로 설정되며, 언제든 순정 런처로 복원할 수 있습니다.

---

## 🎮 리모컨 단축키

| 리모컨 버튼 | 동작 |
| :--- | :--- |
| **방향키 (D-Pad)** | 앱, 채널 및 설정 간 부드럽고 빠른 이동. |
| **OK / 중앙 확인 버튼** | 선택한 앱 실행 또는 IPTV 채널 재생. |
| **OK 길게 누르기** | 앱/채널 관리 메뉴 열기(순서 변경, PIN 잠금, 숨기기, 게임 분류, 앱 정보/삭제). |
| **숫자 키 (`0–9`)** | **빠른 채널 전환**: 홈 화면, IPTV 채널 목록 또는 플레이어에서 채널 번호를 입력해 바로 이동(`OK` 확인, `뒤로` 수정). |
| **메뉴 버튼 (`☰` 세 줄 버튼)** | 화면별 맞춤 동작: **홈 화면**에서는 **설정** 열기, **IPTV**(목록 및 플레이어)에서는 **IPTV 설정** 열기(`INFO`/`GUIDE`는 채널 정보 표시줄 전환), **웹 포털 및 게임**에서는 **가상 마우스 모드** 전환. |
| **음성 / Google 어시스턴트 버튼 (`🎤`)** | 앱 내 음성 검색을 열어 앱, IPTV 채널 또는 웹 검색 실행. |
| **뒤로 가기 버튼** | 이전 화면으로 돌아가거나 앱을 종료하지 않고 안전하게 홈으로 복귀. |

---

## 🛠️ 기술 스택

* **언어**: [Kotlin](https://kotlinlang.org/)
* **UI 프레임워크**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **아키텍처**: MVVM + Coroutines & StateFlow
* **이미지 엔진**: [Coil](https://coil-kt.github.io/coil/)
* **미디어 재생**: [Media3 / ExoPlayer](https://developer.android.com/media/media3)

---

## 📄 라이선스 및 저작권

디자인 및 개발: **© Osama Salsa**.  
이 저장소는 공식 빌드된 릴리스 APK, 다국어 문서, 스크린샷 및 검증된 OTA 자동 업데이트 매니페스트(`version.json`)를 호스팅합니다.

<div align="center">
  <sub>전 세계에 깔끔하고 빠르며 광고 없는 Android TV 경험을 제공하기 위해 ❤️로 제작되었습니다.</sub>
</div>
