# Hướng dẫn kiểm thử

AuctionPro sử dụng JUnit 5 và Mockito cho test tự động. Các bài test hiện bao phủ service layer, DAO SQLite, socket server/client và một số tiện ích UI.

## Chạy test

Chạy toàn bộ test:

```bash
mvn test
```

Chạy giống CI, bao gồm verify và checkstyle:

```bash
mvn clean verify
```

Trên Linux headless, JavaFX cần display ảo:

```bash
xvfb-run -a mvn clean verify
```

GitHub Actions đã chạy Maven dưới `xvfb-run` để `SocketClientIntegrationTest` có thể khởi động JavaFX toolkit mà không gặp lỗi `Unable to open DISPLAY`.

## Nhóm test chính

- `server/src/test/java/com/auction/server/service`: kiểm thử nghiệp vụ đấu giá, ví, auth, settlement và concurrency.
- `server/src/test/java/com/auction/server/dao/sqlite`: kiểm thử DAO với SQLite thật.
- `server/src/test/java/com/auction/server/socket`: kiểm thử routing, authorization và socket request/response.
- `client/src/test/java/com/auction/client/socket`: kiểm thử `SocketClient` với fake TCP server.
- `client/src/test/java/com/auction/client/util`: kiểm thử logic tiện ích không phụ thuộc UI thật.
- `common/src/test/java`: kiểm thử model inheritance.

## Kiểm thử thủ công trước khi bàn giao

1. Build toàn bộ:

   ```bash
   mvn clean install
   ```

2. Chạy server:

   ```bash
   mvn -pl server exec:java
   ```

3. Chạy ít nhất hai client:

   ```bash
   mvn -pl client javafx:run
   ```

4. Kiểm tra các luồng:

   - Đăng nhập bằng `admin`, `seller01`, `bidder01`.
   - Seller tạo phiên đấu giá mới.
   - Bidder nạp tiền và đặt giá.
   - Mở hai client cùng xem một phiên để xác nhận giá cập nhật realtime.
   - Admin hủy một phiên `OPEN` hoặc `RUNNING`.
   - Tắt server trong lúc client đang mở để xác nhận client chuyển về `DISCONNECTED`.

## Lưu ý

Client hiện không có retry reconnect hoặc silent re-authentication tự động. Khi server bị tắt, các request đang chờ sẽ fail, token phía client bị xóa và người dùng cần đăng nhập lại sau khi server chạy lại.
