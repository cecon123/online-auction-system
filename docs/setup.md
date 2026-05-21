# Hướng dẫn cài đặt và chạy dự án

Tài liệu này mô tả cách chuẩn bị môi trường, build và chạy AuctionPro trên máy local.

## Yêu cầu

- JDK 21
- Maven 3.8+
- Windows, macOS hoặc Linux desktop nếu chạy JavaFX client

Nếu chạy test JavaFX trên Linux server/headless, cần `xvfb`.

## Build

Tại thư mục gốc của repository:

```bash
mvn clean install
```

Nếu chỉ muốn tải dependency và kiểm tra biên dịch nhanh:

```bash
mvn clean install -DskipTests
```

## Chạy server

```bash
mvn -pl server exec:java
```

Server mặc định:

- Socket server: `localhost:8080`
- Asset server: `localhost:8081`
- Database: `auction.db`
- Upload directory: `uploads`

## Chạy client

Mở terminal khác:

```bash
mvn -pl client javafx:run
```

Có thể mở nhiều client cùng lúc để kiểm tra realtime bidding.

## Dữ liệu mẫu

Server tự tạo schema từ `server/src/main/resources/db/schema.sql`. Khi bảng `users` đang trống, server nạp dữ liệu mẫu từ `server/src/main/resources/db/seed.sql`.

Tài khoản demo:

| Username | Password | Vai trò |
| --- | --- | --- |
| `admin` | `123456` | `ADMIN` |
| `seller01` | `123456` | `SELLER` |
| `seller02` | `123456` | `SELLER` |
| `bidder01` | `123456` | `BIDDER` |
| `bidder02` | `123456` | `BIDDER` |

Nếu cần chạy server với database sạch, tắt server rồi xóa file `auction.db`.

## Cấu hình

Cấu hình mặc định nằm trong `server/src/main/resources/application.properties`. Code đọc cấu hình qua Java system properties trước, sau đó mới dùng file properties.

| Property | Mặc định | Mô tả |
| --- | --- | --- |
| `server.port` | `8080` | TCP socket server port |
| `server.asset.port` | `8081` | HTTP asset server port |
| `server.asset.dir` | `uploads` | Thư mục lưu ảnh upload |
| `database.url` | `jdbc:sqlite:auction.db` | SQLite database URL |
| `database.enableWal` | `true` | Bật WAL mode |
| `database.busyTimeoutMs` | `5000` | Timeout khi database đang bận |

Ví dụ ghi đè cấu hình:

```bash
mvn -pl server exec:java -Dserver.port=9090 -Ddatabase.url=jdbc:sqlite:auction-demo.db
```

Để bỏ qua seed data:

```bash
mvn -pl server exec:java -Dauction.skip.seed=true
```
