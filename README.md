# AuctionPro - Online Auction System

AuctionPro là dự án bài tập lớn môn Lập trình nâng cao của nhóm 4 thành viên. Hệ thống mô phỏng một nền tảng đấu giá trực tuyến dạng desktop, trong đó nhiều client JavaFX kết nối tới một server TCP để đăng nhập, quản lý phiên đấu giá, đặt giá theo thời gian thực và xử lý ví điện tử.

Repository: [https://github.com/cecon123/online-auction-system](https://github.com/cecon123/online-auction-system)

## 1. Phạm vi hệ thống

AuctionPro tập trung vào luồng đấu giá nội bộ chạy local/demo:

- Người dùng đăng ký, đăng nhập và sử dụng hệ thống theo vai trò `BIDDER`, `SELLER`, `ADMIN`.
- Seller tạo, chỉnh sửa và quản lý phiên đấu giá.
- Bidder xem danh sách phiên đấu giá, tham gia phòng live bidding, đặt giá thủ công và cấu hình auto-bid.
- Server cập nhật giá thầu realtime tới các client đang theo dõi phiên đấu giá.
- Ví điện tử quản lý `balance` và `lockedBalance` để phong tỏa tiền khi người dùng đang dẫn đầu.
- Admin theo dõi người dùng, phiên đấu giá và xử lý các trường hợp bất thường.

Hệ thống được thiết kế để phục vụ demo học phần, chưa triển khai như một dịch vụ production internet-facing.

## 2. Thành viên

| Thành viên | Phụ trách chính |
| --- | --- |
| Huy | Kiến trúc tổng thể, bảo mật, concurrency, review |
| Mạnh | SQLite DAO/Repository, backend unit test, CI/CD |
| Linh | Login/Register UI, dashboard, socket client foundation |
| Hải Anh | Live bidding UI, seller screens, realtime chart, UI integration |

## 3. Công nghệ và môi trường

| Nhóm | Công nghệ |
| --- | --- |
| Ngôn ngữ | Java 21 |
| Build | Maven multi-module |
| Client | JavaFX 21, FXML, CSS, Ikonli |
| Server | TCP Socket, newline-delimited JSON |
| Database | SQLite JDBC |
| Serialization | Gson |
| Logging | SLF4J + Logback |
| Security | BCrypt password hashing |
| Testing | JUnit 5, Mockito |
| CI/CD | GitHub Actions |

Yêu cầu cài đặt:

- JDK 21
- Maven 3.8+
- Hệ điều hành có môi trường đồ họa để chạy JavaFX client: Windows, macOS hoặc Linux desktop

## 4. Cấu trúc module

```text
online-auction-system/
├── common/   Model, DTO, enum và protocol dùng chung
├── server/   Socket server, router, service layer, DAO, SQLite schema
├── client/   JavaFX UI, controller, client service, socket client
├── docs/     Tài liệu kỹ thuật, báo cáo và hướng dẫn demo
└── .github/  GitHub Actions workflow
```

| Module | Vai trò |
| --- | --- |
| `common` | Chia sẻ domain model, DTO, enum và `Request`/`Response` protocol giữa client và server. |
| `server` | Xử lý xác thực, đấu giá, ví, thông báo realtime, quản trị và lưu trữ SQLite. |
| `client` | Ứng dụng desktop JavaFX giao tiếp với server qua `SocketClient`. |

## 5. Build và vị trí file JAR

Build toàn bộ project:

```bash
mvn clean package
```

Sau khi build thành công, các file executable JAR nằm tại:

| Artifact | Vị trí | Lệnh chạy |
| --- | --- | --- |
| Server fat JAR | `server/target/auction-server.jar` | `java -jar server/target/auction-server.jar` |
| Client fat JAR | `client/target/auction-client.jar` | `java -jar client/target/auction-client.jar` |

Nếu muốn chạy cả verify/checkstyle như CI:

```bash
mvn clean verify
```

Trên Linux headless, JavaFX test cần display ảo:

```bash
xvfb-run -a mvn clean verify
```

## 6. Chạy Server và Client

Chạy từ thư mục gốc repository.

1. Build JAR:

   ```bash
   mvn clean package
   ```

2. Mở terminal 1 và chạy server:

   ```bash
   java -jar server/target/auction-server.jar
   ```

3. Mở terminal 2 và chạy client:

   ```bash
   java -jar client/target/auction-client.jar
   ```

4. Để demo nhiều người dùng, mở thêm terminal và chạy lại client:

   ```bash
   java -jar client/target/auction-client.jar
   ```

Server mặc định lắng nghe socket tại port `8080` và asset server tại port `8081`.

Ghi đè cấu hình server bằng Java system properties:

```bash
java -Dserver.port=9090 -Ddatabase.url=jdbc:sqlite:auction-demo.db -jar server/target/auction-server.jar
```

Trong quá trình phát triển vẫn có thể chạy bằng Maven:

```bash
mvn -pl server exec:java
mvn -pl client javafx:run
```

## 7. Tài khoản demo

Khi database trống, server tự tạo schema và nạp dữ liệu mẫu từ `server/src/main/resources/db/seed.sql`.

| Username | Password | Vai trò |
| --- | --- | --- |
| `admin` | `123456` | `ADMIN` |
| `seller01` | `123456` | `SELLER` |
| `seller02` | `123456` | `SELLER` |
| `bidder01` | `123456` | `BIDDER` |
| `bidder02` | `123456` | `BIDDER` |

## 8. Chức năng đã hoàn thành

- Đăng ký, đăng nhập và phân quyền theo vai trò.
- Hash mật khẩu bằng BCrypt trước khi lưu vào SQLite.
- Seller tạo, cập nhật và quản lý phiên đấu giá.
- Bidder xem danh sách phiên, đặt giá thủ công và cấu hình auto-bid.
- Live bidding room hiển thị lịch sử giá, biểu đồ và realtime update.
- Ví điện tử với cơ chế phong tỏa/hoàn trả tiền khi có lượt bid mới.
- Admin quản lý người dùng, trạng thái tài khoản và phiên đấu giá.
- TCP socket protocol dùng newline-delimited JSON, request/response bất đồng bộ bằng `requestId`.
- Broadcast realtime event cho bid update, danh sách phiên đấu giá, danh sách người dùng và thông báo hệ thống.
- Xử lý concurrent bidding bằng lock theo từng `auctionId` và transaction SQLite.
- Bộ test tự động cho service, DAO SQLite, socket, concurrency và util phía client.
- GitHub Actions chạy `mvn clean verify` với display ảo cho JavaFX test.

## 9. Báo cáo và video demo

- Báo cáo PDF: [docs/pdf/auctionpro-report.pdf](docs/pdf/auctionpro-report.pdf)
- Video demo: TODO - cập nhật link video demo tối đa 3 phút sau khi nhóm upload.

Kịch bản video đề xuất:

1. Chạy server bằng `java -jar server/target/auction-server.jar`.
2. Chạy ít nhất hai client bằng `java -jar client/target/auction-client.jar`.
3. Đăng nhập seller để tạo hoặc mở phiên đấu giá.
4. Đăng nhập hai bidder, cùng tham gia một phiên và đặt giá cạnh tranh.
5. Demo realtime update/concurrent bidding hoặc xử lý lỗi khi đặt giá không hợp lệ.

## 10. Tài liệu tham khảo trong repo

- [Deployment guide](docs/deployment.md)
- [Release guide](docs/release-guide.md)
- [Project overview](docs/overview.md)
- [Setup guide](docs/setup.md)
- [User manual](docs/user-manual.md)
- [Admin guide](docs/admin-guide.md)
- [Testing guide](docs/testing.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Socket protocol](docs/protocol.md)
- [Class diagram](docs/class-diagram.md)
- [Database ERD](docs/database-erd.md)
- [Realtime architecture](docs/architecture/realtime.md)
- [Database/backend flow](docs/architecture/database-backend.md)

## 11. Trạng thái nộp bài

- Nhánh nộp cuối cùng theo yêu cầu học phần: `main`.
- Deadline commit cuối: 23:59, ngày 31/05/2026.
- Trước khi nộp cần merge đầy đủ source code và tài liệu cuối cùng vào `main`.
- Video demo cần được quay/upload và cập nhật link vào README trước khi nộp.

## 15. Task Board

- [x] Chuẩn hóa README theo checklist nộp bài.
- [x] Bổ sung cấu hình build executable JAR cho server/client.
- [x] Thêm báo cáo PDF trong repo.
- [ ] Cập nhật link video demo cuối cùng.
