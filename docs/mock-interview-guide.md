# Tài liệu Ôn tập Vấn đáp Bài tập lớn LTNC 2026

Chào mừng cả nhóm đến với bộ tài liệu ôn tập vấn đáp chuyên sâu cho dự án **AuctionPro**. Tài liệu này được biên soạn cực kỳ chi tiết nhằm giúp các thành viên đối phó với những câu hỏi khó và hóc búa nhất từ giảng viên.

## 1. Hướng dẫn ôn tập chiến lược

- **Hiểu sâu hơn thuộc lòng:** Giảng viên thường hỏi "Tại sao?" thay vì "Cái gì?". Hãy tập trung vào các giải thích về "Lý do lựa chọn giải pháp" trong tài liệu.
- **Đối chiếu mã nguồn:** Trong mỗi chủ đề, tôi đã cung cấp đường dẫn đến các file code quan trọng. Bạn BẮT BUỘC phải mở code đó ra xem để có thể "chỉ tay vào code" khi trình bày.
- **Thực hành Mock Interview:** Bộ câu hỏi ở cuối mỗi file là tập hợp các tình huống thực tế đã xảy ra trong các buổi bảo vệ. Hãy thử tự trả lời và đối chiếu với gợi ý.

## 2. Danh mục các Chủ đề Chuyên sâu (Click để đọc)

1. [**Chủ đề 1: Kiến trúc Tổng thể & Lập trình mạng**](interview/01-architecture-and-network.md)
   - Phân tích Component, Luồng dữ liệu qua Socket, Giao thức Newline-delimited JSON và Token-based Auth.
2. [**Chủ đề 2: Xử lý Đa luồng & Concurrency**](interview/02-concurrency-and-multithreading.md)
   - Giải phẫu Race Condition, chiến thuật Khóa mịn (Fine-grained Lock) với `ReentrantLock` và xử lý `Platform.runLater()` trong JavaFX.
3. [**Chủ đề 3: OOP & Design Patterns**](interview/03-oop-and-design-patterns.md)
   - Chỉ ra 4 tính chất OOP trong thực tế, đi sâu vào cấu trúc Singleton (Double-checked locking), Factory Method và Observer (Push notification).
4. [**Chủ đề 4: Thuật toán Nâng cao (Logic thông minh)**](interview/04-advanced-algorithms.md)
   - Giải mã thuật toán Auto-Bidding (Proxy bidding) không đệ quy, Anti-Sniping (Gia hạn thời gian) và Realtime Price Curve.
5. [**Chủ đề 5: Cơ sở dữ liệu, Bảo mật & Testing**](interview/05-database-and-testing.md)
   - Sơ đồ ERD, cơ chế SQLite WAL Mode, bảo mật mật khẩu BCrypt (Salt) và quy trình Unit Test tự động hóa.

---

## 3. Các File Code "Sống còn" cần nắm vững

| Thành phần | Đường dẫn file chính | Nội dung cần nhớ |
|---|---|---|
| **Mạng Server** | `server/.../socket/ClientHandler.java` | Cách Server đọc từng dòng JSON. |
| **Logic Đấu giá** | `server/.../service/BidService.java` | Nơi thực hiện Lock và kiểm tra giá. |
| **Quản lý Khóa** | `server/.../concurrency/AuctionLockManager.java` | Cách Map các Lock theo Auction ID. |
| **Database** | `server/.../dao/Database.java` | Singleton và cấu hình WAL mode. |
| **Model** | `common/.../model/Item.java` | Tính trừu tượng (Abstract class). |
| **UI Realtime** | `client/.../util/NotificationManager.java` | Cách nhận tin nhắn và `runLater`. |

---
*A+ LTNC :))*