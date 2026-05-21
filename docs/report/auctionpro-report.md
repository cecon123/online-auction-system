# Báo cáo bài tập lớn LTNC - AuctionPro

**Môn học:** Lập trình nâng cao  
**Dự án:** AuctionPro - Online Auction System  
**Nhóm:** 4 thành viên  
**Repository:** https://github.com/cecon123/online-auction-system

## 1. Mục tiêu và phạm vi

AuctionPro mô phỏng hệ thống đấu giá trực tuyến dạng desktop theo mô hình client-server. Mục tiêu chính là xây dựng một ứng dụng Java hoàn chỉnh có giao diện người dùng, xử lý dữ liệu bền vững, giao tiếp mạng, phân quyền, cập nhật realtime và kiểm thử tự động cho các luồng nghiệp vụ quan trọng.

Phạm vi thực hiện tập trung vào demo local: một server TCP xử lý nghiệp vụ và nhiều client JavaFX kết nối đồng thời. Hệ thống hỗ trợ ba vai trò `BIDDER`, `SELLER`, `ADMIN`; đấu giá thủ công và auto-bid; quản lý ví điện tử; broadcast cập nhật giá theo thời gian thực; và quản trị người dùng/phiên đấu giá.

## 2. Kiến trúc tổng thể

```text
JavaFX Client(s)
  |  Request/Response JSON over TCP
  v
SocketServer -> RequestRouter -> Service Layer -> DAO Layer -> SQLite
       |              |               |
       |              |               +-- AuthService, AuctionService, BidService, WalletService
       |              +-- Auth/Auction/Bid/Admin/Wallet handlers
       +-- NotificationService -> realtime events back to subscribed clients
```

`common` chứa DTO, enum, model và protocol dùng chung. `client` triển khai giao diện JavaFX, controller, service phía client và `SocketClient`. `server` triển khai socket listener, router, service nghiệp vụ, DAO SQLite, schema/seed data và cơ chế xử lý đồng thời.

Luồng request tiêu chuẩn: client tạo `requestId`, gửi một dòng JSON tới server, `RequestRouter` chuyển tới handler phù hợp, service validate và cập nhật database, sau đó trả `Response` có cùng `requestId`. Với realtime event, server dùng `NotificationService` gửi `BID_UPDATE`, `AUCTION_LIST_UPDATED`, `USER_LIST_UPDATED` hoặc `SYSTEM_NOTIFICATION` tới các client liên quan.

## 3. Chức năng đạt được và hướng giải quyết

| Nhóm chức năng | Kết quả đạt được | Hướng giải quyết và lý do |
| --- | --- | --- |
| Xác thực và phân quyền | Đăng ký, đăng nhập, role `BIDDER`/`SELLER`/`ADMIN`, khóa tài khoản | Dùng BCrypt để hash mật khẩu, token phiên do server quản lý, router kiểm tra quyền trước khi xử lý nghiệp vụ |
| Quản lý đấu giá | Seller tạo/sửa phiên; bidder xem danh sách, xem chi tiết, đặt giá | Tách `AuctionService`, DAO và DTO để client/server giao tiếp qua protocol ổn định |
| Live bidding realtime | Nhiều client nhận cập nhật giá và trạng thái phiên | Dùng TCP socket hai chiều và event listener phía client; tránh polling để phản hồi nhanh hơn |
| Ví điện tử | Theo dõi `balance`, `lockedBalance`, phong tỏa và hoàn tiền khi bị vượt giá | Cập nhật ví trong transaction cùng bid để giữ nhất quán dữ liệu |
| Concurrent bidding | Hạn chế race condition khi nhiều người đặt giá cùng lúc | Khóa theo `auctionId` bằng `AuctionLockManager`, kết hợp SQLite transaction và kiểm tra idempotency bằng `requestId` |
| Auto-bid | Bidder đặt mức tối đa để hệ thống tự tăng giá theo bước tối thiểu | Lưu rule theo user/auction, xử lý trong service để đảm bảo cùng quy tắc validation như đặt giá thủ công |
| Admin | Quản lý người dùng và phiên đấu giá bất thường | Tách handler/service admin, chỉ cho phép role `ADMIN` truy cập |
| Kiểm thử và CI | Unit/integration/concurrency tests, GitHub Actions verify | JUnit 5/Mockito cho service, SQLite test thật cho DAO, socket integration test, `xvfb-run` cho JavaFX test trên Linux headless |

## 4. Đóng gói và chạy demo

Project dùng Maven multi-module với Java 21. Lệnh build:

```bash
mvn clean package
```

Artifact sau khi build:

- Server fat JAR: `server/target/auction-server.jar`
- Client fat JAR: `client/target/auction-client.jar`

Thứ tự chạy demo:

```bash
java -jar server/target/auction-server.jar
java -jar client/target/auction-client.jar
```

Để demo nhiều người dùng, mở thêm terminal và chạy lại client JAR. Server mặc định dùng socket port `8080`, asset port `8081`, SQLite database `auction.db` và thư mục upload `uploads/`.

## 5. Kết luận

AuctionPro hoàn thành các yêu cầu cốt lõi của một hệ thống đấu giá desktop: giao diện người dùng, giao tiếp mạng, lưu trữ, xác thực, realtime update, xử lý đồng thời và kiểm thử tự động. Phần quan trọng nhất về kỹ thuật là đảm bảo tính nhất quán khi nhiều client cùng đặt giá: server gom toàn bộ validation/cập nhật vào service, khóa theo từng phiên đấu giá và phát event sau khi transaction thành công. Cách làm này giữ client đơn giản, server là nguồn dữ liệu tin cậy và phù hợp với phạm vi bài tập lớn.
