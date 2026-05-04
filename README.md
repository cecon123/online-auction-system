# Online Auction System - AuctionPro

> Bài tập lớn Lập trình nâng cao 2026: xây dựng hệ thống đấu giá trực tuyến theo kiến trúc **Client-Server**, sử dụng **Java 21**, **JavaFX**, **Socket JSON**, **SQLite**, **Maven multi-module**, **JUnit 5** và **Git/GitHub**.

---

## 1. Thông tin dự án

- **Tên dự án:** Online Auction System / AuctionPro
- **Repository:** `https://github.com/cecon123/online-auction-system`
- **Môn học:** Lập trình nâng cao - LTNC 2026
- **Mô hình:** Client-Server + MVC
- **Ngôn ngữ:** Java 21
- **GUI:** JavaFX + FXML + CSS
- **Giao tiếp:** TCP Socket + JSON
- **Database:** SQLite
- **Build tool:** Maven multi-module
- **Testing:** JUnit 5, JaCoCo
- **CI/CD:** GitHub Actions
- **Quản lý mã nguồn:** Git + Pull Request review

---

## 2. Current Progress

### 2.1 Trạng thái hiện tại

Dự án đã hoàn thành phần lớn nội dung **W6 - Khởi động, Thiết kế OOP & Bắt đầu JavaFX** và hiện chuyển sang:

```text
W7 - Concurrency & Observer Pattern
```

### 2.2 Đã hoàn thành

- [x] Tạo GitHub repository.
- [x] Thiết lập branch `main` và `dev`.
- [x] Thiết lập Maven multi-module:
  - `common`
  - `server`
  - `client`
- [x] Cấu hình JDK 21.
- [x] Cấu hình JavaFX client.
- [x] Client chạy được bằng:

```bash
mvn -pl client javafx:run
```

- [x] Server chạy được bằng:

```bash
mvn -pl server exec:java
```

- [x] Build toàn bộ project bằng:

```bash
mvn clean install
```

- [x] Tạo nền JSON protocol:
  - `MessageType`
  - `Request<T>`
  - `Response<T>`
- [x] Tạo socket server mock:
  - `SocketServer`
  - `ClientHandler`
  - `RequestRouter`
  - `JsonMapper`
- [x] Test thủ công socket JSON bằng `ncat` với request `LOGIN`.
- [x] Tạo SQLite foundation:
  - `Database`
  - `SchemaInitializer`
  - `application.properties`
  - `schema.sql`
- [x] Tạo `UserDao` và `SQLiteUserDao`.
- [x] Tạo unit test cho `SQLiteUserDao`.
- [x] Tạo common domain model:
  - `Entity`
  - `User`, `Bidder`, `Seller`, `Admin`
  - `Item`, `Electronics`, `Art`, `Vehicle`
  - `Auction`
  - `BidTransaction`
  - `AutoBidRule`
- [x] Tạo `docs/class-diagram.md`.
- [x] Tạo Factory Method:
  - `ItemFactory`
- [x] Tạo `docs/design-patterns.md`.

### 2.3 Đang làm tiếp

- [ ] W7: `AuctionService.placeBid()` dùng `ReentrantLock`.
- [ ] W7: Observer skeleton cho realtime bidding.
- [ ] W7: `ItemDao`, `AuctionDao`, `BidDao`.
- [ ] W7: JavaFX AppShell, Dashboard, AuctionList mock UI.
- [ ] W7: AuctionDetail, LiveBidding, Seller screens mock UI.

---

## 3. Thành viên và phân công chính

| Vai trò | Thành viên | Phụ trách chính |
|---|---|---|
| Backend 1 / Lead | Huy | Kiến trúc tổng thể, Maven multi-module, JSON protocol, socket server, auth, auction core, concurrency, review tích hợp |
| Backend 2 | Mạnh | DAO/Repository, SQLite, scheduler, realtime backend, custom exceptions, unit test backend, CI/CD |
| Frontend 1 | Linh | Login/Register, dashboard, auction list, AppShell, layout chung, CSS JavaFX |
| Frontend 2 | Hải Anh | Auction detail, live bidding screen, seller screens, realtime chart, admin UI tối giản |

Lead không có nghĩa là code hết. Lead chịu trách nhiệm khóa kiến trúc, protocol, tiêu chuẩn merge và đảm bảo mọi module tích hợp được.

> Quy tắc quan trọng: Mỗi thành viên phải hiểu toàn bộ codebase. Nếu bất kỳ thành viên nào không giải thích được bất kỳ phần mã nguồn nào, toàn nhóm có nguy cơ bị 0 điểm. Vì vậy mọi PR cần review chéo và mọi người cần đọc code của nhau.

---

## 4. Mục tiêu chức năng

### 4.1 Chức năng bắt buộc

- Đăng ký / đăng nhập tài khoản.
- Role người dùng:
  - `BIDDER`: tham gia đấu giá.
  - `SELLER`: đăng sản phẩm và tạo phiên đấu giá.
  - `ADMIN`: quản lý user, auction và hủy phiên nếu cần.
- Quản lý sản phẩm đấu giá:
  - Thêm / sửa / xóa sản phẩm.
  - Tạo phiên đấu giá.
  - Theo dõi danh sách sản phẩm của seller.
- Tham gia đấu giá:
  - Xem danh sách phiên đấu giá.
  - Xem chi tiết sản phẩm.
  - Đặt giá cao hơn giá hiện tại.
  - Kiểm tra bid hợp lệ.
  - Cập nhật người đang dẫn đầu.
- Kết thúc phiên đấu giá:
  - Tự động đóng phiên khi hết giờ.
  - Xác định người thắng.
  - Chuyển trạng thái phiên: `OPEN -> RUNNING -> FINISHED -> PAID/CANCELED`.
- Xử lý lỗi:
  - Sai username/password.
  - Bid thấp hơn giá hiện tại.
  - Bid khi auction đã đóng.
  - Seller tự bid vào auction của mình.
  - Lỗi dữ liệu, lỗi kết nối socket.
- Realtime update:
  - Client đang xem một auction nhận `BID_UPDATE` ngay khi có bid mới.
  - Không dùng polling liên tục.
- Concurrency:
  - Nhiều bidder đặt giá cùng lúc không được gây lost update.
  - Không rollback giá.
  - Không có hai người cùng thắng.
- OOP:
  - Encapsulation.
  - Inheritance.
  - Polymorphism.
  - Abstraction.
- Design Patterns:
  - Singleton.
  - Factory Method.
  - Observer.
- Unit test và CI/CD:
  - JUnit cho logic quan trọng.
  - GitHub Actions chạy test khi push / pull request.

### 4.2 Chức năng nâng cao nếu kịp

Ưu tiên theo thứ tự:

1. **Bid History Visualization**: JavaFX LineChart hiển thị giá theo thời gian thực.
2. **Anti-sniping**: nếu có bid trong X giây cuối thì tự động gia hạn thêm Y giây.
3. **Auto-Bidding**: user đặt `maxBid`, `increment`, hệ thống tự tăng giá thay user.

---

## 5. Kiến trúc tổng thể

```text
JavaFX Client
    |
    | TCP Socket + JSON
    v
Auction Server
    |
    v
Controller Layer
    |
    v
Service Layer
    |
    v
DAO / Repository Layer
    |
    v
SQLite Database
```

Nguyên tắc bắt buộc:

- `client` không được truy cập SQLite trực tiếp.
- `client` chỉ giao tiếp với `server` qua `SocketClient` và JSON protocol.
- `server` là nơi duy nhất xử lý nghiệp vụ, kiểm tra quyền, cập nhật database và quyết định kết quả đấu giá.
- Các DTO, enum, protocol class và model dùng chung đặt trong module `common`.
- UI JavaFX tách theo MVC: FXML là View, Controller xử lý UI, client service gọi socket.
- Server tách theo Controller -> Service -> DAO.

---

## 6. Module dependency rule

```text
common  <- không phụ thuộc module nào
server  -> phụ thuộc common
client  -> phụ thuộc common
server  ✗ không phụ thuộc client
client  ✗ không phụ thuộc server
```

Sơ đồ phụ thuộc:

```text
        common
        /    \
       v      v
    server   client
```

Không được làm:

```java
// Sai trong client
import com.auction.server.service.AuthService;
import com.auction.server.dao.UserDao;

// Sai trong server
import com.auction.client.controller.LoginController;
```

Client chỉ gọi server qua socket:

```java
SocketClient.send(Request<?> request);
```

---

## 7. Cấu trúc dự án hiện tại và dự kiến

Lưu ý: README này tách rõ phần **đã có** và **dự kiến**. Một số package trong cây dưới đây là mục tiêu W7-W10, có thể chưa tồn tại ngay ở thời điểm hiện tại.

```text
online-auction-system/
├── README.md
├── pom.xml
├── .gitignore
├── .editorconfig
├── checkstyle.xml                    # Planned / W9 nếu chưa có
├── LICENSE                           # Optional
│
├── .github/
│   └── workflows/
│       └── maven.yml                 # Planned / W9 nếu chưa có
│
├── docs/
│   ├── class-diagram.md              # Done
│   ├── design-patterns.md            # Done
│   ├── protocol.md                   # Planned
│   ├── architecture.md               # Planned
│   ├── database-schema.md            # Planned
│   ├── git-workflow.md               # Planned
│   ├── test-plan.md                  # Planned
│   ├── demo-script.md                # Planned
│   └── ui-design.md                  # Planned
│
├── common/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/auction/common/
│       │   ├── enums/
│       │   │   ├── Role.java
│       │   │   ├── AuctionStatus.java
│       │   │   ├── ItemType.java
│       │   │   ├── ResponseStatus.java       # Planned nếu cần
│       │   │   └── BidType.java              # Planned nếu cần
│       │   │
│       │   ├── protocol/
│       │   │   ├── MessageType.java
│       │   │   ├── Request.java
│       │   │   ├── Response.java
│       │   │   └── ErrorResponse.java        # Planned nếu cần
│       │   │
│       │   ├── dto/                          # Planned W8-W10
│       │   │   ├── auth/
│       │   │   ├── user/
│       │   │   ├── item/
│       │   │   ├── auction/
│       │   │   ├── bid/
│       │   │   └── dashboard/
│       │   │
│       │   ├── model/
│       │   │   ├── Entity.java
│       │   │   ├── User.java
│       │   │   ├── Bidder.java
│       │   │   ├── Seller.java
│       │   │   ├── Admin.java
│       │   │   ├── Item.java
│       │   │   ├── Electronics.java
│       │   │   ├── Art.java
│       │   │   ├── Vehicle.java
│       │   │   ├── Auction.java
│       │   │   ├── BidTransaction.java
│       │   │   └── AutoBidRule.java
│       │   │
│       │   ├── exception/                    # Planned W8
│       │   └── util/                         # Planned
│       │
│       └── test/java/com/auction/common/
│           ├── model/
│           └── protocol/
│
├── server/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/auction/server/
│       │   │   ├── ServerMain.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   └── AppProperties.java
│       │   │   │
│       │   │   ├── socket/
│       │   │   │   ├── SocketServer.java
│       │   │   │   ├── ClientHandler.java
│       │   │   │   └── RequestRouter.java
│       │   │   │
│       │   │   ├── controller/               # Planned W8-W10
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── ItemController.java
│       │   │   │   ├── AuctionController.java
│       │   │   │   └── BidController.java
│       │   │   │
│       │   │   ├── service/                  # Planned W7-W10
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── ItemService.java
│       │   │   │   ├── AuctionService.java
│       │   │   │   ├── BidService.java
│       │   │   │   └── BroadcastService.java
│       │   │   │
│       │   │   ├── dao/
│       │   │   │   ├── Database.java
│       │   │   │   ├── SchemaInitializer.java
│       │   │   │   ├── UserDao.java
│       │   │   │   ├── ItemDao.java          # Planned W7
│       │   │   │   ├── AuctionDao.java       # Planned W7
│       │   │   │   ├── BidDao.java           # Planned W7
│       │   │   │   └── sqlite/
│       │   │   │       ├── SQLiteUserDao.java
│       │   │   │       ├── SQLiteItemDao.java      # Planned W7
│       │   │   │       ├── SQLiteAuctionDao.java   # Planned W7
│       │   │   │       └── SQLiteBidDao.java       # Planned W7
│       │   │   │
│       │   │   ├── factory/
│       │   │   │   └── ItemFactory.java
│       │   │   │
│       │   │   ├── observer/                 # Planned W7/W10
│       │   │   │   ├── AuctionObserver.java
│       │   │   │   ├── AuctionSubject.java
│       │   │   │   └── AuctionEventType.java
│       │   │   │
│       │   │   ├── scheduler/                # Planned W7-W9
│       │   │   │   └── AuctionScheduler.java
│       │   │   │
│       │   │   ├── security/                 # Planned W8
│       │   │   │   ├── PasswordHasher.java
│       │   │   │   ├── SessionManager.java
│       │   │   │   └── PermissionChecker.java
│       │   │   │
│       │   │   ├── concurrency/              # Planned W7
│       │   │   │   ├── LockRegistry.java
│       │   │   │   └── AuctionLockManager.java
│       │   │   │
│       │   │   └── util/
│       │   │       ├── JsonMapper.java
│       │   │       └── ResponseFactory.java  # Planned
│       │   │
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/
│       │           ├── schema.sql
│       │           ├── seed.sql              # Planned
│       │           └── test-seed.sql         # Planned
│       │
│       └── test/java/com/auction/server/
│           ├── dao/
│           ├── factory/
│           ├── service/                      # Planned
│           ├── scheduler/                    # Planned
│           └── concurrency/                  # Planned
│
└── client/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/auction/client/
        │   │   ├── ClientMain.java
        │   │   ├── app/                      # Planned
        │   │   ├── network/                  # Planned W9-W10
        │   │   ├── controller/               # Planned W7-W10
        │   │   ├── service/                  # Planned W9-W10
        │   │   ├── viewmodel/                # Planned
        │   │   ├── component/                # Planned
        │   │   └── util/                     # Planned
        │   │
        │   └── resources/
        │       ├── fxml/
        │       │   ├── LoginView.fxml
        │       │   ├── RegisterView.fxml
        │       │   ├── AppShell.fxml
        │       │   ├── DashboardView.fxml
        │       │   ├── AuctionListView.fxml
        │       │   ├── AuctionDetailView.fxml
        │       │   ├── LiveBiddingView.fxml
        │       │   ├── SellerCenterView.fxml
        │       │   ├── CreateAuctionView.fxml
        │       │   └── AdminPanelView.fxml
        │       ├── css/
        │       ├── images/
        │       └── fonts/
        │
        └── test/java/com/auction/client/
```

---

## 8. OOP Model

### 8.1 Class hierarchy

```text
Entity (abstract)
├── User (abstract)
│   ├── Bidder
│   ├── Seller
│   └── Admin
│
├── Item (abstract)
│   ├── Electronics
│   ├── Art
│   └── Vehicle
│
├── Auction
├── BidTransaction
└── AutoBidRule
```

### 8.2 Relationship summary

```text
Seller 1 ---- * Item
Seller 1 ---- * Auction
Item   1 ---- 1 Auction
Auction 1 --- * BidTransaction
Bidder 1 ---- * BidTransaction
Auction 1 --- * AutoBidRule
Bidder 1 ---- * AutoBidRule
```

### 8.3 Tài liệu liên quan

```text
docs/class-diagram.md
```

---

## 9. Design Patterns

### 9.1 Singleton

Hiện tại:

```text
Database.getInstance()
```

Vị trí:

```text
server/src/main/java/com/auction/server/dao/Database.java
```

Mục đích:

- Tập trung cấu hình SQLite.
- Bật `foreign_keys`, `busy_timeout`, WAL mode ở một nơi.
- DAO không tự tạo database manager riêng.
- Client không truy cập database.

Lưu ý: `Database` là Singleton manager, không giữ một `Connection` global dùng chung mãi mãi. DAO lấy connection khi cần và đóng bằng try-with-resources.

### 9.2 Factory Method

Hiện tại:

```text
ItemFactory.create(...)
```

Vị trí:

```text
server/src/main/java/com/auction/server/factory/ItemFactory.java
```

Mapping:

```text
ItemType.ELECTRONICS -> Electronics
ItemType.ART         -> Art
ItemType.VEHICLE     -> Vehicle
```

Mục đích:

- Tập trung logic tạo concrete `Item`.
- Tránh lặp `if/else` trong service/controller.
- Dễ mở rộng khi thêm loại sản phẩm mới.

### 9.3 Observer

Trạng thái: Planned W7/W10.

Dự kiến:

```text
AuctionObserver
AuctionSubject
BroadcastService
ClientConnectionRegistry
ClientHandler
```

Flow dự kiến:

```text
BidService.placeBid()
-> BroadcastService.broadcastBidUpdate(...)
-> ClientHandler.sendBroadcast(...)
-> JavaFX Client receives BID_UPDATE
-> Platform.runLater(...) updates UI
```

---

## 10. JSON Protocol

### 10.1 Request format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "token": "session-token",
  "data": {
    "auctionId": 1,
    "amount": 1500000
  }
}
```

### 10.2 Response format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "success": true,
  "message": "Bid accepted",
  "data": {
    "auctionId": 1,
    "currentPrice": 1500000,
    "highestBidderId": 2
  }
}
```

### 10.3 Broadcast event format

```json
{
  "type": "BID_UPDATE",
  "requestId": null,
  "success": true,
  "message": "New bid received",
  "data": {
    "auctionId": 1,
    "amount": 1500000,
    "bidderId": 2,
    "timestamp": "2026-05-04T10:30:00"
  }
}
```

### 10.4 Message types dự kiến

```text
AUTH:
- LOGIN
- REGISTER
- LOGOUT

USER:
- GET_PROFILE
- UPDATE_PROFILE
- ADMIN_GET_USERS
- ADMIN_UPDATE_USER_STATUS

ITEM:
- CREATE_ITEM
- UPDATE_ITEM
- DELETE_ITEM
- GET_ITEM
- GET_SELLER_ITEMS

AUCTION:
- CREATE_AUCTION
- UPDATE_AUCTION
- CANCEL_AUCTION
- GET_AUCTION
- GET_AUCTIONS
- GET_AUCTION_DETAIL
- SUBSCRIBE_AUCTION
- UNSUBSCRIBE_AUCTION

BID:
- PLACE_BID
- GET_BID_HISTORY
- BID_UPDATE

SCHEDULER / REALTIME:
- AUCTION_CLOSED
- TIME_EXTENDED

DASHBOARD:
- GET_DASHBOARD
- GET_MARKET_FEED
```

---

## 11. Database Schema

SQLite database file mặc định:

```text
auction.db
```

Cấu hình:

```text
server/src/main/resources/application.properties
```

Ví dụ:

```properties
server.port=8080
database.url=jdbc:sqlite:auction.db
database.enableWal=true
database.busyTimeoutMs=5000
```

Schema hiện tại nằm tại:

```text
server/src/main/resources/db/schema.sql
```

Các bảng chính:

```text
users
aitems / items
auctions
bids
auto_bids
```

Nếu trong `schema.sql` đang dùng `items`, giữ thống nhất là `items`. Không dùng lẫn `aitems`.

Quan hệ chính:

```text
users 1 --- * items
users 1 --- * auctions
items 1 --- 1 auctions
auctions 1 --- * bids
users 1 --- * bids
auctions 1 --- * auto_bids
users 1 --- * auto_bids
```

---

## 12. Hướng dẫn cài đặt và chạy

### 12.1 Yêu cầu môi trường

- JDK 21.
- Maven 3.9+.
- Git.
- JavaFX 21 qua Maven plugin.
- SceneBuilder nếu chỉnh FXML.

### 12.2 Clone repo

```bash
git clone https://github.com/cecon123/online-auction-system.git
cd online-auction-system
```

### 12.3 Build toàn bộ project

```bash
mvn clean install
```

### 12.4 Chạy server

```bash
mvn -pl server exec:java
```

Server mặc định chạy ở port:

```text
8080
```

### 12.5 Chạy client

Mở terminal khác:

```bash
mvn -pl client javafx:run
```

### 12.6 Chạy test

```bash
mvn test
```

Chạy riêng server test:

```bash
mvn -pl server test
```

Chạy riêng common test:

```bash
mvn -pl common test
```

---

## 13. Git Workflow

### 13.1 Branch strategy

```text
main          <- stable/release
dev           <- branch tích hợp chính

feature/project-skeleton-huy
feature/protocol-router-huy
feature/sqlite-userdao-manh
feature/common-model-class-diagram-huy
feature/item-factory-huy
feature/sqlite-item-auction-bid-dao-manh
feature/auction-locking-huy
feature/app-shell-dashboard-linh
feature/auction-detail-live-seller-ui-haianh
```

### 13.2 Quy tắc làm việc

- Không push trực tiếp vào `main`.
- Không push trực tiếp vào `dev` nếu chưa thống nhất.
- Mỗi tính năng làm trên một branch riêng.
- Pull `dev` trước khi tạo branch mới.
- Commit nhỏ, rõ nghĩa.
- PR cần được review trước khi merge.
- Sau khi merge phải chạy lại:

```bash
mvn clean install
```

### 13.3 Tạo branch mới

```bash
git checkout dev
git pull origin dev
git checkout -b feature/<ten-tinh-nang>-<ten-nguoi-lam>
```

Ví dụ:

```bash
git checkout -b feature/sqlite-item-auction-bid-dao-manh
```

### 13.4 Commit

```bash
git status
git add .
git commit -m "feat: add sqlite auction and bid dao"
git push -u origin feature/sqlite-item-auction-bid-dao-manh
```

### 13.5 Merge vào dev

```bash
git checkout dev
git pull origin dev
git merge feature/sqlite-item-auction-bid-dao-manh
mvn clean install
git push origin dev
```

### 13.6 Conventional Commits

```text
feat: thêm chức năng mới
fix: sửa lỗi
refactor: tái cấu trúc không đổi hành vi
test: thêm/sửa test
docs: sửa tài liệu
chore: cấu hình, build, công việc phụ
style: format code
```

Ví dụ:

```bash
git commit -m "feat: add item factory method"
git commit -m "test: add sqlite user dao tests"
git commit -m "docs: update class diagram"
```

---

## 14. Roadmap cập nhật

## W6 - Khởi động & Thiết kế OOP - Done

| Thành viên | Kết quả |
|---|---|
| Huy | Maven multi-module, server/client skeleton, socket JSON protocol mock, common model, class diagram, ItemFactory |
| Mạnh | SQLite schema, Database, SchemaInitializer, AppProperties, UserDao, SQLiteUserDao, DAO test |
| Linh | JavaFX client chạy được, chuẩn bị Login/Register/AppShell |
| Hải Anh | Nghiên cứu UI, chuẩn bị AuctionDetail/LiveBidding/Seller screens |

W6 đã hoàn thành đủ để chuyển sang W7. Các phần đã vượt trước W6 gồm SQLite foundation và socket JSON mock.

## W7 - Concurrency & Observer Pattern - Current

| Thành viên | Việc cần làm |
|---|---|
| Huy | `AuctionService.placeBid()` dùng `ReentrantLock`, `LockRegistry`, `AuctionLockManager`, Observer skeleton, `BroadcastService` |
| Mạnh | `ItemDao`, `AuctionDao`, `BidDao`, `SQLiteItemDao`, `SQLiteAuctionDao`, `SQLiteBidDao`, `AuctionScheduler` skeleton |
| Linh | `AppShell`, `Sidebar`, `TopBar`, `DashboardView`, `AuctionListView`, mock data |
| Hải Anh | `AuctionDetailView`, `LiveBiddingView`, `SellerCenterView`, `CreateAuctionView`, countdown mock |

Branch đề xuất:

```text
feature/sqlite-item-auction-bid-dao-manh
feature/auction-locking-huy
feature/app-shell-dashboard-linh
feature/auction-detail-live-seller-ui-haianh
```

## W8 - Exception Handling & Unit Testing

| Thành viên | Việc cần làm |
|---|---|
| Huy | AuthService thật, PasswordHasher, SessionManager, AuthController, router LOGIN/REGISTER thật |
| Mạnh | Custom exceptions, AuctionServiceTest, BidServiceTest, DAO tests với edge cases |
| Linh | Login/Register validation, error state, shared CSS |
| Hải Anh | Seller screens hoàn chỉnh, form create/edit auction validation |

Custom exceptions dự kiến:

```text
AuctionException
InvalidBidException
AuctionClosedException
AuthenticationException
AuthorizationException
NotFoundException
ValidationException
DataAccessException
```

## W9 - CI/CD & Socket Integration

| Thành viên | Việc cần làm |
|---|---|
| Huy | RequestRouter gọi controller/service thật, ClientHandler session-aware, error response chuẩn JSON |
| Mạnh | Checkstyle Maven, GitHub Actions, seed.sql, serialization hoặc data persistence polish |
| Linh | SocketClient thật cho Login/Register, xử lý lỗi kết nối |
| Hải Anh | AuctionDetail/LiveBidding gọi server thật, PLACE_BID response update UI |

## W10 - Full realtime & GUI hoàn thiện

| Thành viên | Việc cần làm |
|---|---|
| Huy | BroadcastService, subscribe/unsubscribe auction, thread-safe client registry |
| Mạnh | AuctionScheduler thật, OPEN -> RUNNING -> FINISHED, broadcast AUCTION_CLOSED |
| Linh | AuctionList realtime update, dashboard thật |
| Hải Anh | LiveBidding nhận BID_UPDATE, `Platform.runLater()`, LineChart realtime nếu kịp |

## W11-W12 - Tích hợp toàn bộ & E2E Testing

- Chạy Server + 3-4 Client đồng thời.
- Test flow Bidder:

```text
register -> login -> browse auctions -> bid -> receive realtime update -> win/lose result
```

- Test flow Seller:

```text
login -> create item -> create auction -> watch bids -> auction finished -> view result
```

- Fix bug tích hợp.
- Đảm bảo CI xanh.
- Đảm bảo restart server không mất dữ liệu quan trọng.

## W13-W14 - Polish & Chức năng nâng cao

Ưu tiên:

1. Bid History Visualization bằng JavaFX LineChart.
2. Anti-sniping.
3. Auto-Bidding.

Chỉ làm nâng cao khi core system đã ổn.

## W15 - Demo & Chấm điểm

- Chuẩn bị slide.
- Chuẩn bị demo script.
- Mỗi thành viên giải thích được:
  - phần mình code,
  - phần người khác code,
  - kiến trúc tổng thể,
  - concurrency,
  - Observer realtime,
  - DAO/database,
  - JavaFX MVC.

---

## 15. W7 immediate action plan

### 15.1 Mạnh - DAO tiếp theo

Branch:

```bash
git checkout dev
git pull origin dev
git checkout -b feature/sqlite-item-auction-bid-dao-manh
```

Files:

```text
server/src/main/java/com/auction/server/dao/ItemDao.java
server/src/main/java/com/auction/server/dao/AuctionDao.java
server/src/main/java/com/auction/server/dao/BidDao.java
server/src/main/java/com/auction/server/dao/sqlite/SQLiteItemDao.java
server/src/main/java/com/auction/server/dao/sqlite/SQLiteAuctionDao.java
server/src/main/java/com/auction/server/dao/sqlite/SQLiteBidDao.java
server/src/test/java/com/auction/server/dao/sqlite/SQLiteItemDaoTest.java
server/src/test/java/com/auction/server/dao/sqlite/SQLiteAuctionDaoTest.java
server/src/test/java/com/auction/server/dao/sqlite/SQLiteBidDaoTest.java
```

### 15.2 Huy - Concurrency skeleton

Branch:

```bash
git checkout dev
git pull origin dev
git checkout -b feature/auction-locking-huy
```

Files:

```text
server/src/main/java/com/auction/server/concurrency/LockRegistry.java
server/src/main/java/com/auction/server/concurrency/AuctionLockManager.java
server/src/main/java/com/auction/server/service/AuctionService.java
```

Mục tiêu:

```text
- Mỗi auction có một ReentrantLock riêng.
- Không dùng synchronized toàn bộ placeBid().
- Có tryLock timeout để tránh treo server.
- Chuẩn bị cho scheduler chạy song song với bidding.
```

### 15.3 Linh - AppShell + Dashboard + AuctionList mock

Branch:

```bash
git checkout dev
git pull origin dev
git checkout -b feature/app-shell-dashboard-linh
```

Files:

```text
client/src/main/resources/fxml/AppShell.fxml
client/src/main/resources/fxml/DashboardView.fxml
client/src/main/resources/fxml/AuctionListView.fxml
client/src/main/resources/fxml/components/Sidebar.fxml
client/src/main/resources/fxml/components/TopBar.fxml
client/src/main/java/com/auction/client/controller/AppShellController.java
client/src/main/java/com/auction/client/controller/DashboardController.java
client/src/main/java/com/auction/client/controller/AuctionListController.java
client/src/main/java/com/auction/client/util/SceneManager.java
```

### 15.4 Hải Anh - Detail + Live + Seller UI mock

Branch:

```bash
git checkout dev
git pull origin dev
git checkout -b feature/auction-detail-live-seller-ui-haianh
```

Files:

```text
client/src/main/resources/fxml/AuctionDetailView.fxml
client/src/main/resources/fxml/LiveBiddingView.fxml
client/src/main/resources/fxml/SellerCenterView.fxml
client/src/main/resources/fxml/CreateAuctionView.fxml
client/src/main/java/com/auction/client/controller/AuctionDetailController.java
client/src/main/java/com/auction/client/controller/LiveBiddingController.java
client/src/main/java/com/auction/client/controller/SellerCenterController.java
client/src/main/java/com/auction/client/controller/CreateAuctionController.java
```

---

## 16. Testing checklist

Trước khi merge bất kỳ branch nào vào `dev`:

```bash
mvn clean install
```

Nếu branch chỉ sửa common:

```bash
mvn -pl common test
```

Nếu branch sửa server:

```bash
mvn -pl server test
```

Nếu branch sửa client:

```bash
mvn -pl client javafx:run
```

Manual socket test ví dụ:

```bash
ncat localhost 8080
```

Gửi:

```json
{"type":"LOGIN","requestId":"req-001","token":null,"data":{"username":"huy","password":"123456"}}
```

Kỳ vọng server trả JSON response hợp lệ.

---

## 17. Coding conventions

- Java class: `PascalCase`.
- Method/variable: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- DAO naming thống nhất dùng `Dao`, không dùng lẫn `DAO`:
  - `UserDao`
  - `SQLiteUserDao`
  - `AuctionDao`
  - `SQLiteAuctionDao`
- Không để magic string cho message type; dùng `MessageType` enum.
- Không để business logic trong JavaFX Controller.
- Không để SQL trong Service.
- Không để client import server package.
- Không commit file runtime database:
  - `auction.db`
  - `*.db`
  - `*.sqlite`

---

## 18. Presentation notes

Khi bảo vệ, nhóm cần giải thích được:

### Huy

- Kiến trúc Client-Server.
- Maven multi-module.
- JSON protocol.
- SocketServer / ClientHandler / RequestRouter.
- Singleton / Factory / Observer.
- ReentrantLock trong `placeBid()`.

### Mạnh

- SQLite schema.
- DAO layer.
- WAL mode.
- Scheduler.
- Unit test backend.
- Exception handling.

### Linh

- JavaFX MVC.
- FXML Controller.
- Login/Register.
- AppShell / Dashboard / AuctionList.
- Client service gọi socket.

### Hải Anh

- AuctionDetail.
- LiveBidding.
- Realtime update UI bằng `Platform.runLater()`.
- Seller screens.
- LineChart nếu có.

---

## 19. Definition of Done

Một task chỉ được coi là xong khi:

- Code build pass.
- Test liên quan pass.
- Không phá module khác.
- Không import sai dependency rule.
- Có commit rõ nghĩa.
- Có PR hoặc merge được review.
- Nếu thêm file quan trọng, README/docs được cập nhật.
- Người làm giải thích được code của mình.
- Ít nhất một thành viên khác đọc và hiểu phần đó.

