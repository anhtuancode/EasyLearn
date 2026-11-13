# 📖 EasyLearn - Ứng dụng Học Tiếng Anh qua Truyện ngắn & Flashcard SRS

- ✍️ **Tác giả:** Nguyễn Anh Tuấn (anhtuancode)
- 🛠️ **Dự án:** Ứng dụng Android Native (Java) - EasyLearn
- 📬 **Liên hệ:** anhtuancode@gmail.com
- 🐙 **GitHub:** github.com/anhtuancode/EasyLearn-Android

---

## 🚀 Giới thiệu

**EasyLearn** là ứng dụng học tiếng Anh trên hệ điều hành Android, được thiết kế với phương pháp học tập kết hợp giữa **Đọc hiểu theo ngữ cảnh (Contextual Reading)** và **Lặp lại ngắt quãng (Spaced Repetition System - SRS)**. 

Dự án được xây dựng hoàn toàn bằng **Java Native**, sử dụng hệ quản trị cơ sở dữ liệu nội bộ **Room Database (SQLite)**. Ứng dụng nổi bật với kiến trúc hỗ trợ **Multi-User** (nhiều tài khoản trên một thiết bị), xử lý bất đồng bộ mượt mà qua `AppExecutors`, giao diện trực quan theo chuẩn **Material Design** và áp dụng các yếu tố **Gamification** (Game hóa) để duy trì động lực học tập mỗi ngày.

---

## 🧩 Tính năng chính

- ✅ **Cơ sở dữ liệu Đa người dùng (Multi-User Data Isolation):** Tách biệt hoàn toàn tiến độ đọc (`user_story_progress`) và sổ từ vựng (`user_words`) theo từng ID tài khoản. Nhiều người có thể học chung trên 1 máy mà không bị lẫn lộn dữ liệu.
- ✅ **Đọc truyện & Chạm dịch thông minh (Tap-to-Translate):** Nạp sẵn kho 30+ truyện phân cấp từ **A1 đến C2**. Áp dụng `SpannableString` cho phép người dùng chạm vào bất kỳ từ nào để tra nghĩa, nghe phát âm (TTS) và lưu ngay vào sổ tay.
- ✅ **Thuật toán Spaced Repetition (Flashcard SRS):** Hệ thống lật thẻ 3D tự động tính toán thời điểm lặp lại từ vựng. Dựa trên phản hồi *Nhớ (Easy)* hay *Quên (Hard)*, thẻ từ sẽ tự động nhảy Box Level và giãn chu kỳ xuất hiện (1 ngày ➔ 3 ngày ➔ 7 ngày ➔ 14 ngày ➔ 30 ngày).
- ✅ **Hệ thống Game hóa (Gamification):** 
  - Theo dõi Chuỗi học tập liên tục (**Streak**).
  - Thanh tiến trình mục tiêu đọc mỗi ngày (VD: 3, 5, 10 phút/ngày).
  - Tự động đánh giá và **Thăng hạng (Level Up)** (từ A1 lên C2) dựa trên số lượng truyện đã hoàn thành và số từ vựng đã chuyển vào trí nhớ dài hạn.
- ✅ **Giao diện & Trải nghiệm (UI/UX):** Đồng bộ thiết kế bằng `ViewBinding` và `Material Components` (BottomSheet, NestedScrollView, Filter Chips).

---

## ⚙️ Kiến trúc hệ thống

| Thành phần | Công nghệ | Vai trò |
| :--- | :--- | :--- |
| **Nền tảng ứng dụng** | Android Native (Java) | Phát triển logic cốt lõi và vòng đời (Lifecycle) của Activity/Fragment |
| **Giao diện (UI)** | XML, Material Design 3, ViewBinding | Xây dựng màn hình lật thẻ 3D, BottomNavigation, danh sách thẻ truyện |
| **Cơ sở dữ liệu cục bộ** | Room Database (SQLite) | Lưu trữ Offline toàn bộ truyện, người dùng, từ vựng và tiến độ học |
| **Xử lý luồng (Threading)** | AppExecutors (DiskIO, MainThread) | Đảm bảo truy vấn DB và gọi API không làm đơ giao diện người dùng |
| **Xử lý Âm thanh & Dịch** | Text-To-Speech (TTS), REST API | Phát âm chuẩn Anh-Mỹ; tự động cào dữ liệu từ điển API khi chạm từ |
| **Kiến trúc Dữ liệu** | DAO Pattern, Entities | Tách biệt logic truy vấn CSDL (`StoryDao`, `UserWordDao`) khỏi UI |

---

## 🛠️ Thư viện cốt lõi sử dụng trong dự án (build.gradle)

```gradle
// AndroidX & Giao diện cốt lõi
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Room Database (Quản lý SQLite nội bộ)
def room_version = "2.6.1"
implementation "androidx.room:room-runtime:$room_version"
annotationProcessor "androidx.room:room-compiler:$room_version"

// Bật tính năng ViewBinding trong android {}
buildFeatures {
    viewBinding true
}
