# AuctionPro - Online Auction System

AuctionPro là một hệ thống đấu giá trực tuyến chuyên nghiệp được xây dựng trên nền tảng Java, áp dụng kiến trúc **Client-Server** với giao thức truyền tải JSON qua Socket. Dự án được thiết kế theo tiêu chuẩn công nghiệp, chú trọng vào tính concurrency (xử lý đồng thời), bảo mật và hiệu năng cao.

## 🚀 Tính năng chính

- **Đấu giá thời gian thực (Real-time Bidding):** Cập nhật giá và lịch sử bid tức thì thông qua Socket.
- **Tự động đấu giá (Auto-bidding):** Hệ thống thông minh tự động đặt giá dựa trên ngân sách và bước giá tối đa của người dùng.
- **Quản lý đa vai trò:**
  - **Bidder:** Tìm kiếm, xem chi tiết và tham gia đấu giá.
  - **Seller:** Tạo, quản lý và chỉnh sửa các phiên đấu giá của riêng mình.
  - **Admin:** Quản trị người dùng, theo dõi hệ thống và xử lý các vấn đề phát sinh.
- **Ví điện tử (Wallet):** Quản lý số dư, thực hiện nạp/rút và tự động khóa quỹ (locked balance) khi tham gia đấu giá để đảm bảo tính thanh khoản.
- **Bảo mật:** Băm mật khẩu bằng BCrypt, xác thực Token-based đơn giản.

## 🏗️ Kiến trúc kỹ thuật

- **Backend:** Java 21, SQLite (optimistic locking), SLF4J + Logback.
- **Frontend:** JavaFX 21, Ikonli (icons), CSS modern styling.
- **Networking:** TCP Socket, Newline-delimited JSON Protocol.
- **Build System:** Maven Multi-module.
- **CI/CD:** GitHub Actions integration.

## 🛠️ Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Yêu cầu hệ thống
- **Java JDK 21** trở lên.
- **Maven 3.8+**.
- Hệ điều hành: Windows, macOS, hoặc Linux.

### 2. Cài đặt các thư viện phụ thuộc
Trước khi chạy ứng dụng lần đầu, hãy thực hiện build toàn bộ dự án:
```bash
mvn clean install -DskipTests
```

### 3. Chạy Server
Server quản lý logic nghiệp vụ, database SQLite và các kết nối Socket.
```bash
mvn -pl server exec:java
```
*Mặc định Server chạy tại port 8080.*

### 4. Chạy Client
Khởi động giao diện người dùng (mỗi lệnh sẽ mở một cửa sổ ứng dụng mới).
```bash
mvn -pl client javafx:run
```

## 📋 Các lệnh Maven hữu ích

### Chạy Unit Test
Thực hiện kiểm tra toàn diện logic nghiệp vụ và Concurrency:
```bash
mvn test
```
*Dự án sử dụng **Mockito** để viết Unit Test cô lập logic (Service layer) và **JUnit 5** cho Integration Test (DAO layer).*

## 🧪 Hệ thống Kiểm thử (Testing)

Hệ thống được đảm bảo tính ổn định thông qua bộ test bao phủ các luồng quan trọng:
- **Unit Test (Mockito):** Kiểm thử cô lập logic nghiệp vụ của `BidService`, `AuctionService`, `AuthService`, `WalletService`. Giả lập (mock) các DAO để kiểm tra edge-cases (số dư không đủ, đấu giá đã kết thúc, anti-sniping...).
- **Integration Test (SQLite):** Kiểm tra tính đúng đắn của các câu lệnh SQL và tính toàn vẹn dữ liệu trong database thực tế.
- **Concurrency Test:** Kiểm tra khả năng xử lý đồng thời khi có hàng trăm lượt bid gửi đến cùng một lúc (Sử dụng `CountDownLatch` và `ExecutorService`).

## 📂 Tài liệu Dự án (Full Documentation)

Hệ thống cung cấp bộ tài liệu toàn diện phục vụ cho việc sử dụng, phát triển và triển khai:

### 📖 Dành cho Người dùng & Quản trị
- **[Hướng dẫn Sử dụng](docs/user-manual.md):** Quy trình nghiệp vụ cho Bidder, Seller.
- **[Hướng dẫn Admin](docs/admin-guide.md):** Công cụ dành riêng cho Quản trị viên.

### 🏗️ Kiến trúc & Kỹ thuật
- **[Tổng quan Dự án](docs/overview.md):** Mục tiêu và công nghệ sử dụng.
- **[Cấu trúc Thư mục](docs/architecture/folder-structure.md):** Giải thích chi tiết các module.
- **[Xác thực & Phân quyền](docs/architecture/auth.md):** Luồng Login, Roles và Permission.
- **[Giao thức API (Socket)](docs/protocol.md):** Chi tiết cấu trúc JSON, MessageTypes và Examples.
- **[Kiến trúc Realtime](docs/architecture/realtime.md):** Cơ chế Socket, Event Flow và Xử lý đa luồng.
- **[Quản lý Trạng thái](docs/architecture/state-management.md):** Đồng bộ dữ liệu giữa Client và Server.
- **[Database & Backend Flow](docs/architecture/database-backend.md):** Mô hình dữ liệu và quy trình Service.

### 🛠️ Phát triển & Triển khai
- **[Hướng dẫn Cài đặt](docs/setup.md):** Build project và chạy local.
- **[Cho Thành viên mới](docs/onboarding.md):** Git workflow, Coding conventions.
- **[Hướng dẫn Kiểm thử](docs/testing.md):** Quy trình Unit test và Manual test.
- **[Khắc phục Sự cố](docs/troubleshooting.md):** Các lỗi thường gặp và cách xử lý.
- **[Hướng dẫn Triển khai](docs/deployment.md):** Đóng gói JAR và cấu hình Production.

---
© 2026 AuctionPro Team. Sản phẩm phục vụ mục đích học tập và nghiên cứu Lập trình nâng cao.
