# Các Mẫu Thiết kế (Design Patterns) - Hệ thống Đấu giá Trực tuyến

Tài liệu này giải thích các mẫu thiết kế (design patterns) được triển khai trong hệ thống nhằm đảm bảo khả năng mở rộng, an toàn đa luồng và mã nguồn sạch.

---

## 1. Singleton (Đơn khởi tạo)

Được sử dụng để quản lý các tài nguyên dùng chung và đảm bảo các điểm truy cập toàn cục duy nhất.

### Các triển khai cụ thể:
- **`Database.getInstance()`**: Cấu hình kết nối SQLite tập trung. Đảm bảo chế độ WAL (Write-Ahead Logging) được bật và các ràng buộc khóa ngoại được thực thi nhất quán.
- **`JsonMapper.getInstance()`**: Chia sẻ một đối tượng `Gson` duy nhất được cấu hình với các bộ chuyển đổi `LocalDateTime` tùy chỉnh trong toàn bộ ứng dụng (cả Client và Server).
- **`NotificationService.getInstance()`**: Quản lý việc đăng ký của khách hàng và phát sóng (broadcasting) tin nhắn thời gian thực từ một điểm duy nhất.
- **`SessionManager.getInstance()`**: Danh mục trung tâm cho các token hoạt động của người dùng, bao gồm TTL 2 giờ và invalidation khi logout.
- **`AuctionLockManager.getInstance()`**: Cung cấp quyền truy cập vào danh mục khóa (lock registry) toàn cục để kiểm soát đồng thời.

---

## 2. Factory Method (Phương thức Nhà máy)

Được sử dụng để tách biệt logic khởi tạo đối tượng khỏi các dịch vụ nghiệp vụ.

### Triển khai cụ thể:
- **`ItemFactory.create(ItemDto)`**: Tạo ra các lớp con cụ thể (`Electronics`, `Art`, `Vehicle`) dựa trên enum `ItemType` được cung cấp trong DTO.

### Lợi ích:
- **Loose Coupling (Phụ thuộc lỏng)**: `AuctionService` không cần biết các hàm khởi tạo (constructor) cụ thể của từng loại mặt hàng.
- **Khả năng mở rộng**: Việc thêm một loại mặt hàng mới chỉ yêu cầu cập nhật nhà máy (factory) và tạo một lớp con mới.

---

## 3. Observer (Người quan sát)

Được sử dụng cho cơ chế đấu giá thời gian thực cốt lõi.

### Triển khai cụ thể:
- **Subject (Chủ thể)**: `NotificationService` (Server). Nó duy trì một bản đồ (map) từ `auctionId` đến một tập hợp các `PrintWriter` (những người quan sát).
- **Observers (Người quan sát)**: Các trình xử lý kết nối socket của các client khác nhau.

### Luồng hoạt động:
1.  Client gửi yêu cầu `SUBSCRIBE_AUCTION` cho ID #5.
2.  `NotificationService` thêm `PrintWriter` của client đó vào danh sách theo dõi của ID #5.
3.  Khi có một lệnh đặt giá được thực hiện, `BidService` gọi `notificationService.broadcast(5, BID_UPDATE, data)`.
4.  Tất cả các client đã đăng ký sẽ nhận được bản cập nhật JSON ngay lập tức.
5.  Giao diện người dùng (UI) của client cập nhật bằng cách sử dụng `Platform.runLater()`.

---

## 4. Producer-Consumer (Người sản xuất - Người tiêu dùng)

Được sử dụng trong JavaFX Client để xử lý các tin nhắn socket bất đồng bộ.

### Triển khai cụ thể:
- **Producer (Người sản xuất)**: Luồng chạy nền `ListenThread` trong `SocketClient` đợi tin nhắn từ server.
- **Consumer (Người tiêu dùng)**: Luồng ứng dụng JavaFX (`JavaFX Application Thread`) tiêu thụ các tin nhắn này để cập nhật giao diện người dùng.

---

## 5. Tổng kết các Mẫu thiết kế

| Mẫu thiết kế | Lớp triển khai | Mục tiêu chiến lược |
|---|---|---|
| **Singleton** | `Database`, `JsonMapper`, `NotificationService`, `AuctionLockManager` | Hiệu quả tài nguyên và tính nhất quán toàn cục. |
| **Factory Method** | `ItemFactory` | Trừu tượng hóa việc tạo đối tượng (Tuân thủ SRP & OCP). |
| **Observer** | `NotificationService` | Thông báo "Push" thời gian thực mà không cần polling. |
| **Producer-Consumer** | `SocketClient` + `Platform.runLater` | Tách biệt I/O mạng khỏi việc dựng giao diện UI. |
| **DAO** | `UserDao`, `AuctionDao`, `BidDao` | Cô lập logic SQL khỏi logic nghiệp vụ (Business Logic). |
| **DTO** | `Request`, `Response`, `AuctionDto` | Truyền tải dữ liệu an toàn và hiệu quả giữa các module. |
