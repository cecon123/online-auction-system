# Hướng dẫn đóng gói và chạy demo

AuctionPro là dự án bài tập lớp, nên tài liệu này tập trung vào đóng gói và chạy demo local/thử nghiệm. Hệ thống chưa được thiết kế như một dịch vụ production internet-facing.

## Đóng gói

```bash
mvn clean package
```

Sau khi package:

- Server shaded jar: `server/target/auction-server.jar`
- Client artifact: `client/target/client-1.0.0.jar`

Server jar có manifest main class và có thể chạy trực tiếp. Client JavaFX nên chạy bằng Maven plugin trong môi trường phát triển; nếu muốn phân phối client độc lập, cần bổ sung cấu hình packaging/runtime image riêng cho JavaFX.

## Chạy server jar

```bash
java -jar server/target/auction-server.jar
```

Ghi đè cấu hình bằng system properties:

```bash
java -Dserver.port=9090 -Ddatabase.url=jdbc:sqlite:auction-demo.db -jar server/target/auction-server.jar
```

## Chạy client khi demo

Khuyến nghị:

```bash
mvn -pl client javafx:run
```

## Thư mục dữ liệu

Khi chạy ở thư mục gốc project:

```text
auction.db
uploads/
```

Cần sao lưu cả database SQLite và thư mục upload nếu muốn giữ dữ liệu demo sau mỗi lần chạy.
