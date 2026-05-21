# Hướng dẫn đóng gói và chạy demo

AuctionPro là dự án bài tập lớp chạy theo mô hình local client-server. Tài liệu này mô tả cách đóng gói executable JAR và chạy demo bằng lệnh `java -jar`.

## Đóng gói

Chạy tại thư mục gốc repository:

```bash
mvn clean package
```

Sau khi package thành công:

- Server fat JAR: `server/target/auction-server.jar`
- Client fat JAR: `client/target/auction-client.jar`

Cả hai JAR đều có manifest main class và có thể chạy trực tiếp bằng `java -jar`.

## Chạy server

Mở terminal thứ nhất:

```bash
java -jar server/target/auction-server.jar
```

Server mặc định:

- Socket port: `8080`
- Asset HTTP port: `8081`
- SQLite database: `auction.db`
- Upload directory: `uploads/`

Ghi đè cấu hình bằng system properties:

```bash
java -Dserver.port=9090 -Ddatabase.url=jdbc:sqlite:auction-demo.db -jar server/target/auction-server.jar
```

## Chạy client

Mở terminal thứ hai:

```bash
java -jar client/target/auction-client.jar
```

Để demo nhiều người dùng, mở thêm terminal và chạy lại cùng lệnh client:

```bash
java -jar client/target/auction-client.jar
```

## Chạy trong môi trường phát triển

Nếu đang phát triển và muốn dùng Maven plugin:

```bash
mvn -pl server exec:java
mvn -pl client javafx:run
```

## Thư mục dữ liệu

Khi chạy ở thư mục gốc project, server tạo/cập nhật:

```text
auction.db
uploads/
```

Cần sao lưu cả SQLite database và thư mục upload nếu muốn giữ dữ liệu demo sau mỗi lần chạy.
