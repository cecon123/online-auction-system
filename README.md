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
- **Standards:** Tuân thủ Google Java Style, tích hợp CI/CD GitHub Actions.

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

### Kiểm tra Tiêu chuẩn Code (Checkstyle)
Dự án áp dụng Google Java Style. Kiểm tra xem mã nguồn có vi phạm quy tắc không:
```bash
mvn checkstyle:check
```

### Tự động định dạng Code
Sử dụng plugin để tự động sửa lỗi thụt lề (indentation) và sắp xếp import theo chuẩn Google:
```bash
mvn fmt:format
```

### Chạy Unit Test
Thực hiện kiểm tra toàn diện logic nghiệp vụ (Bidding, Seller, Auth, Wallet) và Concurrency:
```bash
mvn test
```

### Đóng gói ứng dụng (Production)
Tạo file JAR thực thi (Fat JAR) cho Server:
```bash
mvn clean package -DskipTests
```
*File JAR của server sẽ nằm tại `server/target/auction-server.jar`.*

## 📂 Cấu trúc thư mục

- `common/`: Chứa các DTO, Enum và Model dùng chung cho cả Client và Server.
- `server/`: Logic xử lý phía Server, Database access (DAO) và Socket Server.
- `client/`: Giao diện JavaFX, Socket Client và xử lý luồng người dùng.
- `docs/`: Tài liệu chi tiết về Protocol, Database ERD và Class Diagram.

---
© 2026 AuctionPro Team. Sản phẩm phục vụ mục đích học tập và nghiên cứu Lập trình nâng cao.
