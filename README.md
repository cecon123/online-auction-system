# AuctionPro - Online Auction System

AuctionPro là dự án bài tập lớn môn Lập trình nâng cao của nhóm 4 thành viên. Dự án mô phỏng một hệ thống đấu giá trực tuyến dạng desktop, sử dụng JavaFX cho client, Java socket cho server và SQLite cho lưu trữ dữ liệu.

Mục tiêu chính của dự án là triển khai trọn vẹn một ứng dụng client-server có đăng nhập theo vai trò, đấu giá thời gian thực, quản lý ví, xử lý đồng thời và kiểm thử tự động cho các luồng nghiệp vụ quan trọng.

## Thành viên

| Thành viên | Phụ trách chính |
| --- | --- |
| Huy | Kiến trúc tổng thể, bảo mật, concurrency, review |
| Mạnh | SQLite DAO/Repository, backend unit test, CI/CD |
| Linh | Login/Register UI, dashboard, socket client foundation |
| Hải Anh | Live bidding UI, seller screens, realtime chart, UI integration |

## Tính năng chính

- Đăng ký, đăng nhập và phân quyền theo vai trò `BIDDER`, `SELLER`, `ADMIN`.
- Người bán tạo, chỉnh sửa và quản lý phiên đấu giá.
- Người thầu xem danh sách, tham gia phòng đấu giá trực tiếp, đặt giá thủ công và cấu hình auto-bid.
- Cập nhật realtime qua TCP socket với newline-delimited JSON.
- Ví điện tử có `balance` và `lockedBalance` để phong tỏa tiền khi người dùng đang dẫn đầu phiên đấu giá.
- Admin theo dõi người dùng, phiên đấu giá và có thể vô hiệu hóa tài khoản hoặc hủy phiên bất thường.
- Server xử lý đồng thời bằng lock theo từng phiên đấu giá, tránh ghi đè khi nhiều người đặt giá cùng lúc.
- Bộ test gồm unit test, integration test SQLite, socket test và concurrency test.

## Kiến trúc

```text
online-auction-system/
├── common/   Shared model, DTO, enum, request/response protocol
├── server/   Socket server, routing, service layer, DAO, SQLite schema
├── client/   JavaFX UI, controllers, client services, socket client
├── docs/     Tài liệu kỹ thuật và hướng dẫn sử dụng
└── .github/  GitHub Actions workflow
```

| Module | Vai trò |
| --- | --- |
| `common` | Chứa domain model, DTO, enum và protocol dùng chung giữa client/server. |
| `server` | Xử lý nghiệp vụ, xác thực, đấu giá, ví, thông báo realtime và SQLite. |
| `client` | Ứng dụng JavaFX giao tiếp với server qua `SocketClient`. |

## Công nghệ

- Java 21
- Maven multi-module
- JavaFX 21
- TCP Socket + newline-delimited JSON
- SQLite JDBC
- Gson
- SLF4J + Logback
- BCrypt
- JUnit 5, Mockito
- GitHub Actions

## Yêu cầu môi trường

- JDK 21
- Maven 3.8+
- Hệ điều hành có môi trường đồ họa nếu chạy JavaFX client: Windows, macOS hoặc Linux desktop

## Chạy dự án

Build toàn bộ project:

```bash
mvn clean install
```

Chạy server:

```bash
mvn -pl server exec:java
```

Chạy client ở một terminal khác:

```bash
mvn -pl client javafx:run
```

Server mặc định lắng nghe socket tại port `8080` và asset server tại port `8081`.

## Tài khoản demo

Khi database trống, server tự tạo schema và nạp dữ liệu mẫu từ `server/src/main/resources/db/seed.sql`.

| Username | Password | Vai trò |
| --- | --- | --- |
| `admin` | `123456` | `ADMIN` |
| `seller01` | `123456` | `SELLER` |
| `seller02` | `123456` | `SELLER` |
| `bidder01` | `123456` | `BIDDER` |
| `bidder02` | `123456` | `BIDDER` |

## Kiểm thử

Chạy toàn bộ test:

```bash
mvn test
```

Chạy cùng bước verify như CI:

```bash
mvn clean verify
```

Trên Linux headless, JavaFX test cần display ảo:

```bash
xvfb-run -a mvn clean verify
```

GitHub Actions đã được cấu hình chạy `mvn clean verify` dưới `xvfb-run` để tránh lỗi `Unable to open DISPLAY` khi test `SocketClientIntegrationTest`.

## Cấu hình server

Các giá trị mặc định nằm trong `server/src/main/resources/application.properties`. Có thể ghi đè bằng Java system properties khi chạy Maven hoặc `java -jar`.

| Property | Mặc định | Mô tả |
| --- | --- | --- |
| `server.port` | `8080` | TCP socket server port |
| `server.asset.port` | `8081` | HTTP asset server port |
| `server.asset.dir` | `uploads` | Thư mục lưu ảnh upload |
| `database.url` | `jdbc:sqlite:auction.db` | SQLite database URL |
| `database.enableWal` | `true` | Bật WAL mode cho SQLite |
| `database.busyTimeoutMs` | `5000` | Timeout khi SQLite bận |

Ví dụ:

```bash
mvn -pl server exec:java -Dserver.port=9090 -Ddatabase.url=jdbc:sqlite:auction-demo.db
```

## Tài liệu

- [Project overview](docs/overview.md)
- [Setup guide](docs/setup.md)
- [User manual](docs/user-manual.md)
- [Admin guide](docs/admin-guide.md)
- [Testing guide](docs/testing.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Socket protocol](docs/protocol.md)
- [Class diagram](docs/class-diagram.md)
- [Database ERD](docs/database-erd.md)
- [Folder structure](docs/architecture/folder-structure.md)
- [Authentication architecture](docs/architecture/auth.md)
- [Realtime architecture](docs/architecture/realtime.md)
- [Database/backend flow](docs/architecture/database-backend.md)
- [State management](docs/architecture/state-management.md)
- [Development guide](docs/development.md)
- [Git workflow](docs/git-workflow.md)

## Trạng thái

Dự án đã hoàn thiện các luồng chính phục vụ demo và đánh giá bài tập lớp. Những thay đổi mới nên được thực hiện trên branch `feature/<tên-người-làm>/<tên-task>`, chạy test trước khi tạo Pull Request vào `dev`.
