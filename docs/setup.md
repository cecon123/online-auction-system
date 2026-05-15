# Hướng dẫn Cài đặt & Chạy dự án (Setup Guide)

## 1. Yêu cầu Tiên quyết
Để chạy được AuctionPro, máy tính của bạn cần cài đặt:
- **Java Development Kit (JDK) 21**: Dự án sử dụng các tính năng mới nhất của Java 21.
- **Apache Maven 3.8+**: Công cụ quản lý dự án và đóng gói.

## 2. Cài đặt Môi trường & Build
Tải mã nguồn và thực hiện build toàn bộ các module:
```bash
git clone <repository-url>
cd online-auction-system
mvn clean install -DskipTests
```
Lệnh này sẽ tải các thư viện phụ thuộc (Gson, Ikonli, SQLite JDBC, BCrypt...) và tạo ra các file `.jar` trong thư mục `target` của từng module.

## 3. Cấu hình Biến môi trường (Environment Variables)
Dự án sử dụng file `application.properties` tại `server/src/main/resources/` cho các cấu hình cơ bản. Bạn có thể ghi đè bằng biến môi trường nếu cần:

| Biến | Mặc định | Mô tả |
|------|----------|-------|
| `SERVER_PORT` | `8080` | Cổng lắng nghe của Socket Server. |
| `DB_URL` | `jdbc:sqlite:auction.db` | Đường dẫn tới file cơ sở dữ liệu SQLite. |
| `ASSET_DIR` | `uploads/` | Thư mục lưu trữ ảnh mặt hàng tải lên. |

## 4. Chạy Dự án Cục bộ (Local Run)

### Bước 1: Khởi động Server
Server cần được chạy trước để Client có thể kết nối tới.
```bash
mvn -pl server exec:java
```
Khi thấy dòng log `SocketServer started on port 8080`, backend đã sẵn sàng.

### Bước 2: Khởi động Client
Mở một cửa sổ dòng lệnh mới và chạy:
```bash
mvn -pl client javafx:run
```
Bạn có thể chạy lệnh này nhiều lần để mở đồng thời nhiều Client nhằm kiểm thử tính năng thời gian thực.

## 5. Khởi tạo Dữ liệu (Database Seed)
Hệ thống tự động tạo file `auction.db` và các bảng cần thiết khi chạy lần đầu. Để có dữ liệu mẫu (Admin, Bidders, Auctions), bạn có thể chạy file SQL `server/src/main/resources/db/seed.sql` thông qua một trình quản lý SQLite hoặc để hệ thống tự load nếu có cấu hình tương ứng trong `SchemaInitializer`.
