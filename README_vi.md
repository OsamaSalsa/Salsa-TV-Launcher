[English](README.md) | [العربية](README_ar.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Italiano](README_it.md) | [Русский](README_ru.md) | [Español](README_es.md) | [Português](README_pt.md) | [Türkçe](README_tr.md) | [Polski](README_pl.md) | [Nederlands](README_nl.md) | [Ελληνικά](README_el.md) | [Bahasa Indonesia](README_id.md) | **Tiếng Việt** | [ไทย](README_th.md) | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [हिन्दी](README_hi.md) | [اردو](README_ur.md) | [فارسی](README_fa.md)

<div align="center">

# 🌌 Salsa TV Launcher

**Trải nghiệm tương lai của giải trí gia đình với giao diện TV hiện đại, siêu mượt và đậm chất điện ảnh.**

[![Android](https://img.shields.io/badge/Platform-Android%20TV-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Version](https://img.shields.io/badge/Version-2.1-0EA5E9.svg?style=for-the-badge)](https://github.com/OsamaSalsa/Salsa-TV-Launcher)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![Download APK](https://img.shields.io/badge/Download-APK%20Release-FF0055?style=for-the-badge&logo=github)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

### 📥 [Nhấn vào đây để tải xuống phiên bản APK mới nhất (Sẵn sàng cài đặt)](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk)

<br>

<img src="docs/screenshots/home.png" alt="Salsa TV Launcher Main Preview" width="800"/>

</div>

---

## ✨ Tổng Quan

**Salsa TV Launcher** không chỉ là một màn hình chính thông thường; đây là một hệ sinh thái giải trí toàn diện được thiết kế riêng cho **Android TV và Smart TV Box**. Được xây dựng 100% bằng **Kotlin** và **Jetpack Compose**, Salsa kết hợp phong cách thiết kế "Glassmorphism" mượt mà, đậm chất điện ảnh với các công cụ mạnh mẽ tích hợp sẵn như trình phát **IPTV / Truyền hình trực tiếp**, **Trung tâm Cloud Gaming**, **Kho ứng dụng Salsa Store** và **Mini Downloader** để tải và cài đặt trực tiếp các gói APK.

Được tối ưu hóa chuyên biệt cho điều khiển từ xa của TV (điều hướng D-Pad), mọi hiệu ứng chuyển động, trạng thái tiêu điểm và chuyển cảnh đều đạt tốc độ 60 FPS mượt mà không giật lag.

---

## 🚀 Tính Năng Nổi Bật

### 🆕 Tính Năng Mới ở Phiên Bản 2.1
* **🌐 Trung Tâm 4 Bảng Thông Tin Trực Tiếp**: Truy cập nhanh từ thanh trên cùng vào bốn màn hình có hiệu ứng chuyển động mượt mà:
  * **☀️ Bảng Thời Tiết**: Tự động định vị bằng **GPS** & tìm kiếm thành phố, biểu đồ 24 giờ, độ ẩm/gió/áp suất/UV và dự báo 5 ngày.
  * **📰 Trung Tâm Tin Tức**: Nguồn RSS riêng biệt cho cả **21 ngôn ngữ** qua 5 chuyên mục (**Tin nổi bật, Chính trị, Kinh tế, Công nghệ, Thế giới**) không trùng lặp tiêu đề, tự động chuyển bài mỗi 10 giây.
  * **📈 Kinh Tế & Thị Trường**: Chỉ số chứng khoán toàn cầu, tỷ giá Forex, hàng hóa & tiền số, biểu đồ 5 ngày và lịch kinh tế tùy chỉnh.
  * **⚽ Trung Tâm Bóng Đá (365Scores)**: Tỷ số trực tiếp, lịch đấu hôm nay, bảng xếp hạng, vua phá lưới, danh sách CLB/giải đấu mở rộng và xác suất thắng.
* **🖥️ Trình Bảo Vệ Màn Hình Bảng Trực Tiếp & Tự Động Luân Phiên**: Hiển thị **Thời tiết**, **Tin tức**, **Kinh tế**, **Bóng đá** hoặc **Luân phiên tất cả** (**Tin tức 60s → Thời tiết 10s → Kinh tế 10s → Bóng đá 10s → quay lại Tin tức và tự động chuyển sang chuyên mục tiếp theo**). Chế độ Tin tức riêng cũng tự động qua cả 5 chuyên mục.
* **📡 Bảo Vệ Màn Hình Tin Chữ Phát Sóng (`Broadcast Text Showcase`)**: Dòng tin chuyển động kiểu trường quay trên nền Aurora tối 60%, luân phiên chữ **trắng & vàng**, nhãn `ON AIR`, tùy chỉnh từng dòng chữ, chỉnh thời lượng (`3–15s`) và nhạc nền hoặc MP3 tùy chọn.
* **🎨 Cài Đặt Giao Diện Trực Tiếp (`Live UI Settings HUD`)**: Thanh nổi ngay trên màn hình chính để tùy chỉnh hình nền 4K, độ sáng, vị trí ứng dụng, số cột, độ mờ kính, đồng hồ/ngày/lời chào, thanh trên, thời tiết, lọc ban đêm, Material You và chế độ nhẹ **với xem trước thay đổi trực tiếp ngay lập tức trên màn hình chính**.
* **🧭 Thanh Trên Không Bị Cắt & Động Cơ v2.1 60 FPS**: Biểu tượng luôn hiển thị đầy đủ, APK gọn hơn với chất lượng 4K nguyên vẹn.
* **🌍 Cổng Web Mặc Định & 21 Ngôn Ngữ**: Bao gồm **YouTube TV**, **Google (`google.com`)** và **4 cổng game** (**CrazyGames**, **Poki**, **Playhop**, **EasyGames**), dịch đầy đủ **21 ngôn ngữ**.

### 🖥️ 1. Màn Hình Chính Điện Ảnh & Sống Động
* **Hiệu Ứng Tiêu Điểm Thông Minh:** Các thẻ ứng dụng trở nên sống động khi được chọn với viền phát sáng, đổ bóng mềm mại và phóng to mượt mà.
* **Thanh Tiêu Đề Ngữ Cảnh & Thời Tiết Trực Tiếp:** Hiển thị lời chào giàu cảm xúc theo từng khung giờ trong ngày, đồng hồ thời gian thực (12h/24h), trạng thái mạng (Wi-Fi / Ethernet) và thời tiết địa phương trực tiếp.
* **Yêu Thích & Ghim Nhanh:** Ghim các ứng dụng yêu thích lên thanh truy cập nhanh hoặc sắp xếp lại thứ tự ngay lập tức.
* **Hỗ Trợ 21 Ngôn Ngữ (LTR & RTL):** Bản địa hóa hoàn chỉnh sang **21 ngôn ngữ**, bao gồm Tiếng Việt, Tiếng Anh, Tiếng Ả Rập, Tiếng Đức, Tiếng Pháp, Tiếng Ý, Tiếng Nga, Tiếng Tây Ban Nha, Tiếng Bồ Đào Nha, Tiếng Thổ Nhĩ Kỳ, Tiếng Ba Lan, Tiếng Hà Lan, Tiếng Hy Lạp, Tiếng Indonesia, Tiếng Thái, Tiếng Trung, Tiếng Nhật, Tiếng Hàn, Tiếng Hindi, Tiếng Urdu và Tiếng Ba Tư.

<div align="center">
  <img src="docs/screenshots/home_screen.png" alt="Home Screen" width="48%"/>
  <img src="docs/screenshots/wallpaper_grid.png" alt="Wallpaper and Grid" width="48%"/>
</div>

---

### 📺 2. Trình Phát IPTV & Truyền Hình Trực Tiếp Tích Hợp
* **Hỗ Trợ Danh Sách Phát M3U / M3U8:** Thêm danh sách phát IPTV của riêng bạn hoặc khám phá các kênh truyền hình quốc tế có sẵn.
* **Nhân ExoPlayer (Media3) Nguyên Bản:** Chuyển kênh cực nhanh với sự hỗ trợ cho các luồng HLS và DASH.
* **Lọc Danh Mục & Tìm Kiếm:** Lọc kênh theo Thể thao, Tin tức, Phim ảnh, Thiếu nhi hoặc tìm kiếm tức thì.
* **Kênh Yêu Thích:** Lưu lại các kênh bạn xem nhiều nhất để mở chỉ với một cú nhấp từ màn hình chính.

<div align="center">
  <img src="docs/screenshots/iptv_player.png" alt="IPTV Player" width="750"/>
</div>

---

### 🛍️ 3. Salsa Store (Kho Ứng Dụng Cho TV)
* **Danh Mục Tuyển Chọn Cho Android TV:** Tải xuống và cài đặt các ứng dụng xem phim, trình phát đa phương tiện, trình duyệt và tiện ích tốt nhất được tối ưu hóa cho TV mà không cần tài khoản Google.
* **Tải Xuống & Cài Đặt Trực Tiếp:** Thanh tiến trình thời gian thực tự động kích hoạt trình cài đặt gói của hệ thống.

<div align="center">
  <img src="docs/screenshots/aura_store.png" alt="Salsa Store" width="750"/>
</div>

---

### 🎮 4. Cổng Cloud Gaming & Ứng Dụng Web
* **Chơi Game Không Cần Console:** Truy cập trực tiếp chỉ với một cú nhấp vào **Xbox Cloud Gaming**, **NVIDIA GeForce NOW**, **Amazon Luna** và các cổng game retro/web.
* **Tối Ưu Hóa Cho Tay Cầm (Gamepad):** Kết nối tay cầm Bluetooth và biến chiếc TV của bạn thành trung tâm chơi game thực thụ.

<div align="center">
  <img src="docs/screenshots/gaming_portals.png" alt="Gaming Portals" width="750"/>
</div>

---

### 📥 5. Mini Downloader & Cài Đặt APK Qua URL/Mã Số
* **Tải Qua URL Hoặc Mã Ngắn:** Nhập bất kỳ liên kết `.apk` trực tiếp nào hoặc mã số ngắn bằng bàn phím ảo thân thiện với điều khiển TV.
* **Lịch Sử Tải Xuống:** Quản lý, cài đặt lại hoặc xóa các tệp APK đã tải xuống trước đó một cách dễ dàng.

<div align="center">
  <img src="docs/screenshots/mini_downloader.png" alt="Mini Downloader" width="750"/>
</div>

---

### 🎨 6. Tùy Biến Sâu & Kiểm Soát Của Phụ Huynh
* **Hình Nền 4K & Chủ Đề:** Chọn từ hàng chục hình nền độ phân giải cao (Thiên nhiên, Vũ trụ, Trừu tượng, Cyberpunk, Tối giản) hoặc đặt ảnh tùy chỉnh từ bộ nhớ trong/USB.
* **Chế Độ Biểu Tượng Thanh Trên Cùng:** Chọn hiển thị biểu tượng **Trung Tâm Điều Khiển**, bỏ qua Trung tâm điều khiển để hiển thị **Cài Đặt Trực Tiếp**, hoặc **Hiển Thị Cả Hai Cùng Nhau** trên Màn hình chính.
* **Định Dạng Ngày Kiểu Mỹ & Quốc Tế:** Kiểu Mỹ (`MM/dd/yyyy`, `MMMM d, yyyy`, `EEEE, MMMM d, yyyy`, `EEE, MMM d`), Quốc tế (`dd/MM/yyyy`), ISO (`yyyy-MM-dd`), Châu Âu (`dd.MM.yyyy`) hoặc ẩn.
* **Tỷ Lệ Giao Diện, Màu Sắc Động (Material You) & Tất Cả Ứng Dụng Android (Sideloaded APKs):** Tùy chỉnh số cột, độ trong suốt của thẻ (10%–100%), bật **Màu Sắc Động Material You**, và chuyển đổi giữa chỉ hiển thị ứng dụng TV hoặc **tất cả ứng dụng Android đã cài đặt (bao gồm APK di động)**.
* **Trình Bảo Vệ Màn Hình Điện Ảnh, Video YouTube Tùy Chỉnh & Trình Chiếu Ảnh (`Slideshow`):** Thêm video YouTube tùy chỉnh vào màn hình chờ kèm nút **xem trước trực tiếp**, hoặc trình chiếu ảnh từ thư mục (Bộ nhớ/USB), ảnh đã chọn hoặc bộ sưu tập 4K với thời gian mỗi ảnh (`5s`–`5m`), chuyển động **Ken Burns / Thu Phóng Nhẹ / Tĩnh**, 6 hiệu ứng chuyển cảnh, 7 hiệu ứng hạt (**Tuyết rơi ❄️**, **Đom đóm ✨**, **Mưa 🌧️**, **Lá mùa thu 🍂**, **Hoa anh đào 🌸**, **Ngôi sao ⭐**, **Bokeh 🔮**), đồng hồ góc và nhạc nền êm dịu hoặc MP3 tùy chỉnh.
* **Trình Quản Lý Dung Lượng Ứng Dụng & 8 Phím Tắt Hệ Thống:** Xem tất cả ứng dụng đã cài đặt theo dung lượng (`MB`), mở thông tin hệ thống hoặc **gỡ cài đặt ứng dụng trực tiếp** để giải phóng bộ nhớ TV, cùng 8 phím tắt nhanh (Wi-Fi, Ethernet, Màn hình, Âm thanh, Ngày/Giờ, Cài đặt, Thông tin ứng dụng, Quản lý bộ nhớ).
* **Lá Chắn DNS Hai Chế Độ (AdBlock + Bảo Vệ Gia Đình) & Khóa PIN:** Chọn giữa **Chặn Quảng Cáo & Theo Dõi** hoặc **Chế Độ Bảo Vệ Gia Đình** (chặn trang web người lớn trên toàn hệ thống), và bảo vệ ứng dụng hoặc danh mục IPTV bằng mã PIN 4 chữ số.
* **Tìm Kiếm Giọng Nói (`🎤`), Trợ Lý Giọng Nói Mặc Định Hệ Thống, Dấu Trang Web Thông Minh & Đa Danh Sách IPTV:** Đặt **Salsa TV** làm trợ lý giọng nói mặc định của TV để nút micro trên điều khiển hoạt động trực tiếp; quản lý **nhiều danh sách phát M3U**; dấu trang web tự động tải biểu tượng HD và mở YouTube ở chế độ **YouTube TV Leanback**; hàng ứng dụng gần đây và cập nhật OTA từ GitHub.

---

## 🎮 Hướng Dẫn Phím Tắt Trên Điều Khiển Từ Xa

Salsa TV Launcher được thiết kế để điều khiển dễ dàng bằng bất kỳ điều khiển Android TV tiêu chuẩn nào:

| Nút Trên Điều Khiển | Nơi Sử Dụng | Chức Năng Thực Hiện |
| :--- | :--- | :--- |
| **Nút OK / Chọn (Nhấn nhanh)** | Bất kỳ ứng dụng hoặc thẻ nào | Mở ứng dụng, kênh hoặc cổng thông tin đã chọn. |
| **Nút OK / Chọn (Nhấn giữ)** | Trên biểu tượng ứng dụng | Mở **Menu Tùy Chọn Nhanh** (Ghim/Bỏ ghim, Di chuyển, Ẩn, Khóa PIN, Thông tin ứng dụng, Gỡ cài đặt). |
| **Nút Menu (☰ Ba Gạch)** | Trên ứng dụng hoặc Màn hình chính | Mở menu ngữ cảnh của ứng dụng đang chọn hoặc bảng cài đặt nhanh. |
| **Nút Giọng Nói / Google Assistant (🎤)** | Bất kỳ đâu trong Launcher | Mở **Tìm Kiếm Giọng Nói Toàn Diện** của Salsa để tìm ứng dụng, kênh IPTV hoặc thực hiện lệnh thoại. |
| **Phím Điều Hướng (D-Pad)** | Trên toàn bộ ứng dụng | Di chuyển mượt mà giữa các danh mục, ứng dụng, kênh và nút điều khiển phát. |
| **Nút Quay Lại (Back)** | Trong cửa sổ / trình phát | Đóng các menu đang mở, thoát toàn màn hình hoặc quay lại đầu màn hình chính. |

> 💡 **Mẹo:** Bạn cũng có thể xem toàn bộ hướng dẫn nút bấm này ngay trong ứng dụng tại mục **Cài đặt > Hướng dẫn điều khiển**.

---

## 🛠️ Kiến Trúc & Công Nghệ

* **Ngôn ngữ:** 100% [Kotlin](https://kotlinlang.org/) với Coroutines và StateFlow.
* **Giao diện (UI):** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material Design 3 thích ứng cho TV.
* **Kiến trúc:** MVVM (Model-View-ViewModel) với quản lý trạng thái rõ ràng (`UiState`).
* **Phát đa phương tiện:** [AndroidX Media3 / ExoPlayer](https://developer.android.com/guide/topics/media/media3) cho các luồng HLS/M3U8.
* **Tải hình ảnh:** [Coil](https://coil-kt.github.io/coil/) với bộ nhớ đệm đĩa và RAM được tối ưu hóa.
* **Mạng & Thời tiết:** OkHttp + Open-Meteo API cung cấp dữ liệu thời tiết chính xác không cần khóa API.

---

## 📦 Cài Đặt

### Cách 1: Tải Trực Tiếp Tệp APK (Khuyên dùng)
1. Tải xuống tệp [`salsa-tv-launcher.apk`](https://github.com/OsamaSalsa/Salsa-TV-Launcher/raw/refs/heads/main/salsa-tv-launcher.apk) từ mục Releases.
   👉 **[Mã rút gọn ứng dụng Downloader / Liên kết trực tiếp: aftv.news/5157967](http://aftv.news/5157967)**
2. Chuyển tệp sang Android TV của bạn qua USB, ứng dụng *Send Files to TV* hoặc *Downloader*.
3. Cài đặt APK và cấp các quyền được yêu cầu.
4. Nhấn nút **Home** trên điều khiển và chọn **Salsa TV Launcher** làm màn hình chính mặc định (**"Luôn luôn"**).

### Cách 2: Biên Dịch Từ Mã Nguồn
```bash
git clone https://github.com/OsamaSalsa/Salsa-TV-Launcher.git
cd Salsa-TV-Launcher
./gradlew assembleDebug
```
Tệp APK sau khi biên dịch sẽ nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🤝 Đóng Góp & Giấy Phép

Mọi đóng góp, báo lỗi và đề xuất tính năng mới đều được hoan nghênh! Hãy thoải mái mở *Issue* hoặc gửi *Pull Request*.

Được phân phối theo Giấy phép **MIT**. Xem tệp `LICENSE` để biết thêm chi tiết.
