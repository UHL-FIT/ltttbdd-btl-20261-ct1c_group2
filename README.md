# FlickFind - Ứng dụng tra cứu và đánh giá phim 🎬

FlickFind là một ứng dụng di động Android chuyên biệt giúp người dùng tra cứu thông tin phim ảnh, quản lý danh sách phim yêu thích và tham gia vào cộng đồng đánh giá phim theo thời gian thực. Dự án được xây dựng theo kiến trúc **MVVM** kết hợp **Clean Architecture** và sử dụng hoàn toàn **Jetpack Compose** cho giao diện người dùng.

## 🌟 Tính năng nổi bật

*   **🔍 Tìm kiếm thông minh:** Tìm kiếm phim với cơ chế Debounce, lịch sử tìm kiếm và bộ lọc theo thể loại.
*   **📡 Tích hợp API Thực:** Dữ liệu phim phong phú, chính xác và được cập nhật liên tục từ [TMDB API](https://www.themoviedb.org/).
*   **📱 Giao diện hiện đại (MD3):** Thiết kế theo chuẩn Material Design 3, hỗ trợ chế độ Tối/Sáng (Dark/Light mode) và giao diện linh hoạt.
*   **💬 Cộng đồng Realtime:** Hệ thống Đánh giá sao và Bình luận trực tiếp, phản hồi (reply) và thả tim (like) cập nhật ngay lập tức nhờ Firebase Firestore.
*   **✈️ Chế độ Ngoại tuyến (Offline-First):** Ứng dụng vẫn hoạt động, hiển thị danh sách phim và Watchlist ngay cả khi không có kết nối Internet nhờ hệ thống lưu trữ cục bộ (Room Database).
*   **🔐 Quản lý Tài khoản:** Đăng ký, đăng nhập và quản lý thông tin hồ sơ người dùng (Firebase Auth).

## 🛠 Công nghệ sử dụng

*   **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
*   **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Kiến trúc:** MVVM (Model-View-ViewModel)
*   **Xử lý Bất đồng bộ:** Kotlin Coroutines & Flow (`StateFlow`, `callbackFlow`)
*   **Mạng & API:** [Retrofit2](https://square.github.io/retrofit/) & OkHttp
*   **Database Cục bộ:** [Room Database](https://developer.android.com/training/data-storage/room)
*   **Backend (BaaS):** [Firebase](https://firebase.google.com/) (Authentication & Cloud Firestore)
*   **Tải ảnh:** [Coil](https://coil-kt.github.io/coil/compose/)
*   **Điều hướng:** Navigation Compose

## 🚀 Hướng dẫn Cài đặt & Chạy dự án

1. **Clone repository này về máy:**
   ```bash
   git clone https://github.com/YourUsername/FlickFind.git
   ```
2. **Mở dự án bằng Android Studio:** (Khuyên dùng phiên bản Android Studio Iguana hoặc mới hơn).
3. **Cấu hình API Key:**
   * Ứng dụng yêu cầu API Key của TMDB. Tuy nhiên, API Key mặc định đã được cấu hình sẵn trong mã nguồn. Bạn có thể thay đổi bằng key của bạn trong `TMDBApiService.kt` nếu cần.
4. **Cấu hình Firebase (Tùy chọn nếu muốn dùng backend riêng):**
   * Dự án đã đính kèm file `google-services.json` cơ bản. 
   * Nếu bạn muốn liên kết với project Firebase của riêng mình, hãy tạo một project trên Firebase Console, bật **Authentication (Email/Password)** và **Firestore Database** (cấu hình Rules cho phép đọc/ghi). Sau đó tải file `google-services.json` mới chèn vào thư mục `app/`.
5. **Build và Chạy:** Nhấn nút Run (Shift + F10) để chạy ứng dụng trên máy ảo (Emulator) hoặc thiết bị thật.

## 🤝 Nhóm thực hiện (Nhóm 2)
* Nguyễn Xuân Kiên (Nhóm trưởng)
* Phạm Công Nghĩa
* Trần Thanh Huyền
* Bùi Đình Dũng

## 📜 Giấy phép
Dự án này là bài tập lớn môn Lập trình Mobile và được phát triển phục vụ cho mục đích học tập. Mọi hình ảnh và dữ liệu phim thuộc bản quyền của [The Movie Database (TMDB)](https://www.themoviedb.org/).
