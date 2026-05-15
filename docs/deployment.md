# Hướng dẫn Triển khai (Deployment Guide)

Tài liệu này hướng dẫn cách đóng gói và triển khai hệ thống AuctionPro lên môi trường thực tế hoặc máy chủ tập trung.

## 1. Đóng gói Ứng dụng (Packaging)

Sử dụng Maven để tạo các file thực thi `.jar`:
```bash
mvn clean package -DskipTests
```
Sau lệnh này:
- **Server:** File `auction-server.jar` sẽ được tạo trong `server/target/`. Đây là một "Uber-jar" chứa tất cả các thư viện phụ thuộc.
- **Client:** File `client-1.0.0.jar` sẽ được tạo trong `client/target/`.

## 2. Chuẩn bị Môi trường Máy chủ

### Yêu cầu:
- JRE (Java Runtime Environment) 21.
- Mở cổng Firewall (mặc định 8080) cho lưu lượng TCP.

### Cấu trúc thư mục triển khai khuyến nghị:
```text
/opt/auctionpro/
├── auction-server.jar
├── application.properties
├── uploads/
└── db/
```

## 3. Cấu hình Production

Chỉnh sửa `application.properties` để phù hợp với môi trường production:
```properties
# server.properties
server.port=8080
db.url=jdbc:sqlite:db/auction_prod.db
asset.dir=uploads/
```

## 4. Chạy Ứng dụng

### Chạy Backend (Server)
Sử dụng `nohup` hoặc công cụ quản lý tiến trình (như `systemd` trên Linux) để chạy server ngầm:
```bash
java -jar auction-server.jar
```

### Chạy Frontend (Client)
Người dùng cuối chỉ cần file `client.jar` và môi trường Java 21 để chạy:
```bash
java -jar client.jar
```
*Lưu ý: Đối với môi trường thực tế, Client cần được cấu hình địa chỉ IP của Server thay vì `localhost`.*

## 5. Sao lưu & Bảo trì (Backup)
- **Database:** Chỉ cần sao lưu file `.db` định kỳ. SQLite cho phép sao lưu nóng (Hot backup) mà không cần tắt server.
- **Tài sản (Assets):** Sao lưu thư mục `uploads/` để bảo vệ ảnh mặt hàng của người dùng.
