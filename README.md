# Online Auction System - AuctionPro

> Bài tập lớn Lập trình nâng cao 2026: xây dựng hệ thống đấu giá trực tuyến theo kiến trúc **Client-Server**, sử dụng **Java 21**, **JavaFX/FXML/CSS**, **Socket JSON**, **SQLite**, **Maven multi-module**, **JUnit 5** và **Git/GitHub**.

---

## 1. Thông tin dự án

- **Tên dự án:** Online Auction System / AuctionPro
- **Repository:** `https://github.com/cecon123/online-auction-system`
- **Môn học:** Lập trình nâng cao - LTNC 2026
- **Mô hình:** Client-Server + MVC
- **Ngôn ngữ:** Java 21
- **GUI:** JavaFX + FXML + CSS
- **Giao tiếp:** TCP Socket + newline-delimited JSON
- **Database:** SQLite
- **Build tool:** Maven multi-module
- **Testing:** JUnit 5
- **Quản lý mã nguồn:** Git + GitHub + Pull Request review

---

## 2. Trạng thái hiện tại

Dự án hiện đang ở giai đoạn cuối W6 / đầu W7:

```text
W6 - Khởi động, Thiết kế OOP & JavaFX UI mock: phần lớn đã hoàn thành
W7 - Concurrency, Observer skeleton, DAO mở rộng: đang chuẩn bị triển khai
```

### 2.1 Đã hoàn thành

#### Project foundation

- [x] Tạo Maven multi-module project:
  - `common`
  - `server`
  - `client`
- [x] Cấu hình Java 21.
- [x] Cấu hình JavaFX client.
- [x] Build được toàn bộ project bằng Maven.
- [x] Có `.gitignore`, `.editorconfig`.
- [x] Có tài liệu trong `docs/`.

#### Common module

- [x] Tạo enum dùng chung:
  - `Role`
  - `ItemType`
  - `AuctionStatus`
- [x] Tạo protocol class:
  - `MessageType`
  - `Request<T>`
  - `Response<T>`
- [x] Tạo DTO bước đầu:
  - `LoginRequest`
  - `LoginResponse`
  - `RegisterRequest`
  - `RegisterResponse`
  - `AuctionSummaryDto`
  - `CreateAuctionRequest`
  - `PlaceBidRequest`
  - `PlaceBidResponse`
  - `BidUpdateEvent`
- [x] Tạo domain model OOP:
  - `Entity`
  - `User`, `Bidder`, `Seller`, `Admin`
  - `Item`, `Electronics`, `Art`, `Vehicle`
  - `Auction`
  - `BidTransaction`
  - `AutoBidRule`
- [x] Có unit test bước đầu cho inheritance model.

#### Server module

- [x] Có entry point `ServerMain`.
- [x] Có cấu hình server qua `application.properties`.
- [x] Có `AppProperties` để đọc config.
- [x] Có `Database` Singleton manager cho SQLite connection.
- [x] Có `SchemaInitializer` đọc `db/schema.sql` khi server start.
- [x] Có `UserDao` và `SQLiteUserDao`.
- [x] Có `ItemFactory` cho Factory Method.
- [x] Có socket foundation:
  - `SocketServer`
  - `ClientHandler`
  - `RequestRouter`
  - `JsonMapper`
- [x] Có mock route cho một số message type:
  - `LOGIN`
  - `REGISTER`
  - `GET_AUCTIONS`
  - `GET_AUCTION_DETAIL`
  - `PLACE_BID`
  - `SUBSCRIBE_AUCTION`
  - `UNSUBSCRIBE_AUCTION`
- [x] Có test bước đầu cho DAO user và item factory.

#### Client module

- [x] Có entry point `ClientMain`.
- [x] Có `SceneManager` quản lý chuyển màn.
- [x] Có JavaFX AppShell gồm:
  - Sidebar
  - TopBar
  - Center content area
- [x] Có các màn hình FXML chính:
  - `LoginView.fxml`
  - `RegisterView.fxml`
  - `AppShell.fxml`
  - `BidderDashboardView.fxml`
  - `SellerDashboardView.fxml`
  - `AdminDashboardView.fxml`
  - `AuctionListView.fxml`
  - `AuctionDetailView.fxml`
  - `LiveBiddingView.fxml`
  - `SellerCenterView.fxml`
  - `CreateAuctionView.fxml`
  - `AdminPanelView.fxml`
  - `WalletView.fxml`
  - `MyBidsView.fxml`
- [x] Có controller tương ứng cho các màn hình chính.
- [x] Có CSS giao diện chung tại `client/src/main/resources/css/app.css`.
- [x] Có UI mock theo role:
  - Bidder: xem auction, vào live bidding, my bids, wallet.
  - Seller: seller dashboard, seller center, create auction, wallet.
  - Admin: admin dashboard, admin panel.
- [x] Có LiveBidding mock gồm:
  - current price
  - bid history
  - realtime-like chart
  - manual bid validation mock
  - auto-bidding mock

### 2.2 Chưa hoàn thành / đang planned

Các phần sau **chưa phải logic thật**, cần làm ở W7-W10:

- [ ] `AuthService` thật.
- [ ] `PasswordHasher` thật.
- [ ] `SessionManager` thật.
- [ ] `AuthController`, `AuctionController`, `BidController`.
- [ ] `ItemDao`, `AuctionDao`, `BidDao`.
- [ ] `SQLiteItemDao`, `SQLiteAuctionDao`, `SQLiteBidDao`.
- [ ] `AuctionService` / `BidService` thật.
- [ ] `AuctionLockManager` / `LockRegistry` dùng `ReentrantLock`.
- [ ] `BroadcastService` và Observer realtime thật.
- [ ] Client `SocketClient` thật.
- [ ] Client service layer:
  - `AuthClientService`
  - `AuctionClientService`
  - `BidClientService`
- [ ] Client socket listener nhận server-pushed event.
- [ ] `Platform.runLater()` cho realtime UI update.
- [ ] GitHub Actions CI/CD.
- [ ] Checkstyle.

---

## 3. Thành viên và phân công chính

| Vai trò | Thành viên | Phụ trách chính |
|---|---|---|
| Backend 1 / Lead | Huy | Kiến trúc tổng thể, Maven multi-module, protocol, socket server, factory, auth/service/concurrency/realtime về sau |
| Backend 2 | Mạnh | SQLite schema, DAO/Repository, unit test backend, scheduler, CI/CD |
| Frontend 1 | Linh | Login/Register, dashboard, auction list, AppShell, Sidebar/TopBar, CSS |
| Frontend 2 | Hải Anh | Auction detail, live bidding, seller screens, create auction, realtime chart mock |

Quy tắc quan trọng: **mỗi thành viên phải hiểu toàn bộ codebase**. Lead không có nghĩa là code hết; lead chịu trách nhiệm khóa kiến trúc, protocol, tiêu chuẩn merge và đảm bảo các module tích hợp được.

---

## 4. Kiến trúc tổng thể

```text
JavaFX Client
    |
    | TCP Socket + newline-delimited JSON
    v
Auction Server
    |
    v
Controller Layer        # Planned W8/W9
    |
    v
Service Layer           # Planned W7/W9
    |
    v
DAO / Repository Layer
    |
    v
SQLite Database
```

Nguyên tắc bắt buộc:

- `client` không được truy cập SQLite trực tiếp.
- `client` chỉ giao tiếp với `server` qua socket JSON.
- `server` là nơi xử lý nghiệp vụ, kiểm tra quyền, cập nhật database và quyết định kết quả đấu giá.
- Các DTO, enum, protocol class và model dùng chung đặt trong module `common`.
- UI JavaFX tách theo MVC: FXML là View, Controller xử lý UI và gọi client service.
- Server tách theo Controller -> Service -> DAO.

---

## 5. Module dependency rule

```text
common  <- không phụ thuộc module nào
server  -> phụ thuộc common
client  -> phụ thuộc common
server  ✗ không phụ thuộc client
client  ✗ không phụ thuộc server
```

Sơ đồ:

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

---

## 6. Cấu trúc dự án hiện tại

```text
online-auction-system/
├── README.md
├── pom.xml
├── .gitignore
├── .editorconfig
│
├── docs/
│   ├── class-diagram.md
│   ├── design-patterns.md
│   ├── git-workflow.md
│   └── protocol.md
│
├── common/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/auction/common/
│       │   ├── dto/
│       │   │   ├── auction/
│       │   │   │   ├── AuctionSummaryDto.java
│       │   │   │   └── CreateAuctionRequest.java
│       │   │   ├── auth/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── LoginResponse.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   └── RegisterResponse.java
│       │   │   └── bid/
│       │   │       ├── BidUpdateEvent.java
│       │   │       ├── PlaceBidRequest.java
│       │   │       └── PlaceBidResponse.java
│       │   ├── enums/
│       │   │   ├── AuctionStatus.java
│       │   │   ├── ItemType.java
│       │   │   └── Role.java
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
│       │   └── protocol/
│       │       ├── MessageType.java
│       │       ├── Request.java
│       │       └── Response.java
│       └── test/java/com/auction/common/model/
│           └── ModelInheritanceTest.java
│
├── server/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/auction/server/
│       │   │   ├── ServerMain.java
│       │   │   ├── config/
│       │   │   │   └── AppProperties.java
│       │   │   ├── dao/
│       │   │   │   ├── Database.java
│       │   │   │   ├── SchemaInitializer.java
│       │   │   │   ├── UserDao.java
│       │   │   │   └── sqlite/
│       │   │   │       └── SQLiteUserDao.java
│       │   │   ├── factory/
│       │   │   │   └── ItemFactory.java
│       │   │   ├── socket/
│       │   │   │   ├── SocketServer.java
│       │   │   │   ├── ClientHandler.java
│       │   │   │   └── RequestRouter.java
│       │   │   └── util/
│       │   │       └── JsonMapper.java
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/
│       │           └── schema.sql
│       └── test/java/com/auction/server/
│           ├── dao/sqlite/
│           │   └── SQLiteUserDaoTest.java
│           └── factory/
│               └── ItemFactoryTest.java
│
└── client/
    ├── pom.xml
    └── src/main/
        ├── java/com/auction/client/
        │   ├── ClientMain.java
        │   ├── controller/
        │   │   ├── LoginController.java
        │   │   ├── RegisterController.java
        │   │   ├── AppShellController.java
        │   │   ├── DashboardController.java
        │   │   ├── AuctionListController.java
        │   │   ├── AuctionDetailController.java
        │   │   ├── LiveBiddingController.java
        │   │   ├── SellerCenterController.java
        │   │   ├── CreateAuctionController.java
        │   │   ├── AdminPanelController.java
        │   │   ├── WalletController.java
        │   │   ├── SidebarController.java
        │   │   └── TopBarController.java
        │   └── util/
        │       └── SceneManager.java
        └── resources/
            ├── css/
            │   └── app.css
            └── fxml/
                ├── LoginView.fxml
                ├── RegisterView.fxml
                ├── AppShell.fxml
                ├── BidderDashboardView.fxml
                ├── SellerDashboardView.fxml
                ├── AdminDashboardView.fxml
                ├── AuctionListView.fxml
                ├── AuctionDetailView.fxml
                ├── LiveBiddingView.fxml
                ├── SellerCenterView.fxml
                ├── CreateAuctionView.fxml
                ├── AdminPanelView.fxml
                ├── WalletView.fxml
                ├── MyBidsView.fxml
                └── components/
                    ├── Sidebar.fxml
                    └── TopBar.fxml
```

---

## 7. OOP Model

### 7.1 Class hierarchy

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

### 7.2 User model

```text
User
├── id
├── username
├── passwordHash
├── fullName
├── role
├── active
└── createdAt
```

Subclasses:

```text
Bidder
- canBid()

Seller
- canCreateAuction()

Admin
- canManageSystem()
```

### 7.3 Item model

```text
Item
├── id
├── sellerId
├── itemType
├── name
├── description
├── startingPrice
├── imagePath
└── createdAt
```

Subclasses:

```text
Electronics
- brand
- model

Art
- artist
- material

Vehicle
- manufacturer
- year
```

Nếu branch đồng bộ model đã merge, `Item` sẽ có thêm:

```text
condition
```

### 7.4 Auction model

```text
Auction
├── id
├── itemId
├── sellerId
├── currentPrice
├── highestBidderId
├── startTime
├── endTime
├── status
├── version
└── createdAt
```

Method quan trọng:

```text
isRunningAt(now)
canAcceptBidAt(now)
updateHighestBid(bidderId, amount)
extendEndTimeSeconds(seconds)
```

### 7.5 Relationship summary

```text
Seller 1 ---- * Item
Seller 1 ---- * Auction
Item   1 ---- 1 Auction
Auction 1 --- * BidTransaction
Bidder 1 ---- * BidTransaction
Auction 1 --- * AutoBidRule
Bidder 1 ---- * AutoBidRule
```

---

## 8. Design Patterns

### 8.1 Singleton

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

### 8.2 Factory Method

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

### 8.3 Observer

Trạng thái: **Planned W7/W10**.

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

## 9. JSON Socket Protocol

### 9.1 Quy tắc quan trọng

Tất cả message client-server là **newline-delimited JSON**:

```text
one request = one JSON line
one response = one JSON line
```

Vì `ClientHandler` đọc bằng `readLine()`, JSON gửi qua socket không được pretty-print nhiều dòng.

### 9.2 Request format

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

Khi test bằng `ncat`, gửi trên **một dòng**:

```json
{"type":"PLACE_BID","requestId":"uuid-123","token":"session-token","data":{"auctionId":1,"amount":1500000}}
```

### 9.3 Response format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "success": true,
  "message": "Bid accepted",
  "data": {
    "auctionId": 1,
    "currentPrice": 1500000,
    "highestBidderUsername": "huy",
    "timestamp": "2026-05-04T20:30:00"
  }
}
```

### 9.4 Broadcast event format

```json
{
  "type": "BID_UPDATE",
  "requestId": null,
  "success": true,
  "message": "New bid received",
  "data": {
    "auctionId": 1,
    "bidderUsername": "huy",
    "amount": 1500000,
    "timestamp": "2026-05-04T20:30:00",
    "newEndTime": null
  }
}
```

### 9.5 Message types hiện có

```text
AUTH:
- REGISTER
- LOGIN
- LOGOUT

DASHBOARD:
- GET_DASHBOARD

AUCTION:
- GET_AUCTIONS
- GET_AUCTION_DETAIL
- CREATE_AUCTION
- UPDATE_AUCTION
- CANCEL_AUCTION
- SUBSCRIBE_AUCTION
- UNSUBSCRIBE_AUCTION

ITEM:
- CREATE_ITEM
- UPDATE_ITEM
- DELETE_ITEM

BID:
- PLACE_BID
- GET_BID_HISTORY
- BID_UPDATE

REALTIME:
- AUCTION_CLOSED
- TIME_EXTENDED

ADMIN:
- ADMIN_GET_USERS
- ADMIN_UPDATE_USER_STATUS
- ADMIN_GET_AUCTIONS
```

---

## 10. Database Schema

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

Schema nằm tại:

```text
server/src/main/resources/db/schema.sql
```

Các bảng chính:

```text
users
items
auctions
bids
auto_bids
```

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

Lưu ý kỹ thuật:

- Không commit file runtime database:
  - `auction.db`
  - `*.db`
  - `*.sqlite`
- Nếu thay đổi `schema.sql`, SQLite không tự migrate DB cũ. Khi test local có thể xóa DB cũ:

```bash
rm -f auction.db
```

---

## 11. Client UI hiện tại

### 11.1 Auth UI

Màn hình:

```text
LoginView.fxml
RegisterView.fxml
```

Controller:

```text
LoginController.java
RegisterController.java
```

Trạng thái:

- Login hiện đang mock theo username:
  - username chứa `admin` -> role `ADMIN`
  - username chứa `seller` -> role `SELLER`
  - còn lại -> role `BIDDER`
- Register hiện validate form và hiển thị mock success.
- Chưa gọi server thật.

### 11.2 App shell

Màn hình:

```text
AppShell.fxml
components/Sidebar.fxml
components/TopBar.fxml
```

Controller:

```text
AppShellController.java
SidebarController.java
TopBarController.java
```

Trạng thái:

- Sidebar ẩn/hiện menu theo role.
- TopBar hiển thị avatar, username, role, wallet/balance theo role.
- Center content được thay qua `SceneManager`.

### 11.3 Bidder UI

Màn hình:

```text
BidderDashboardView.fxml
AuctionListView.fxml
AuctionDetailView.fxml
LiveBiddingView.fxml
MyBidsView.fxml
WalletView.fxml
```

Trạng thái:

- Auction list/detail đang mock navigation.
- Live bidding có mock realtime chart, bid history, manual bid, auto-bid.
- Wallet đang mock local balance.
- Chưa lấy dữ liệu thật từ server.

### 11.4 Seller UI

Màn hình:

```text
SellerDashboardView.fxml
SellerCenterView.fxml
CreateAuctionView.fxml
WalletView.fxml
```

Trạng thái:

- Seller center điều hướng sang create auction.
- Create auction validate form:
  - product name
  - item type
  - condition
  - description
  - starting price
  - start time
  - end time
  - image
- Hiện vẫn là mock save.
- Cần nối với `CREATE_AUCTION` sau khi `AuctionClientService` có thật.

### 11.5 Admin UI

Màn hình:

```text
AdminDashboardView.fxml
AdminPanelView.fxml
```

Trạng thái:

- Admin panel đang mock các thao tác:
  - export report
  - view user
  - disable user
  - view auction
  - cancel auction
- Chưa nối với server thật.

---

## 12. Hướng dẫn cài đặt và chạy

### 12.1 Yêu cầu môi trường

- JDK 21.
- Maven 3.9+.
- Git.
- JavaFX qua Maven dependency/plugin.
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

Chạy riêng common test:

```bash
mvn -pl common test
```

Chạy riêng server test:

```bash
mvn -pl server test
```

---

## 13. Manual socket test

Cài `ncat` nếu cần, sau đó chạy server:

```bash
mvn -pl server exec:java
```

Mở terminal khác:

```bash
ncat localhost 8080
```

### Test LOGIN

Gửi một dòng JSON:

```json
{"type":"LOGIN","requestId":"req-login-001","token":null,"data":{"username":"seller01","password":"123456"}}
```

Kỳ vọng server trả response JSON hợp lệ.

### Test PLACE_BID

```json
{"type":"PLACE_BID","requestId":"req-bid-001","token":"mock-session-token","data":{"auctionId":1,"amount":15000}}
```

### Test GET_AUCTIONS

```json
{"type":"GET_AUCTIONS","requestId":"req-auctions-001","token":"mock-session-token","data":null}
```

---

## 14. Git Workflow

### 14.1 Branch strategy

```text
main          <- stable/release/demo branch
dev           <- integration branch
feature/*     <- branch cho từng task
```

### 14.2 Quy tắc làm việc

- Không push trực tiếp vào `main`.
- Không push trực tiếp vào `dev` nếu chưa thống nhất.
- Mỗi tính năng làm trên một branch riêng.
- Pull `dev` trước khi tạo branch mới.
- Commit nhỏ, rõ nghĩa.
- PR cần được review trước khi merge.
- Sau khi merge phải chạy lại `mvn clean install`.

### 14.3 Tạo branch mới

```bash
git checkout dev
git pull origin dev
git checkout -b feature/<ten-task>-<ten-nguoi-lam>
```

Ví dụ:

```bash
git checkout -b feature/sqlite-item-auction-bid-dao-manh
```

### 14.4 Commit

```bash
git status
git add .
git commit -m "feat: add sqlite auction and bid dao"
git push -u origin feature/sqlite-item-auction-bid-dao-manh
```

### 14.5 Merge vào dev

```bash
git checkout dev
git pull origin dev
git merge feature/sqlite-item-auction-bid-dao-manh
mvn clean install
git push origin dev
```

### 14.6 Conventional Commits

```text
feat: thêm chức năng mới
fix: sửa lỗi
refactor: tái cấu trúc không đổi hành vi
test: thêm/sửa test
docs: sửa tài liệu
chore: cấu hình, build, công việc phụ
style: format code
```

---

## 15. Roadmap cập nhật

## W6 - Khởi động & Thiết kế OOP - Done / Mostly Done

| Thành viên | Kết quả |
|---|---|
| Huy | Maven multi-module, server/client skeleton, socket JSON protocol mock, common model, class diagram, ItemFactory |
| Mạnh | SQLite schema, Database, SchemaInitializer, AppProperties, UserDao, SQLiteUserDao, DAO test |
| Linh | JavaFX client, Login/Register, AppShell, Dashboard, AuctionList, Sidebar/TopBar |
| Hải Anh | AuctionDetail, LiveBidding, Seller screens, CreateAuction UI, LineChart/AutoBid mock |

## W7 - Concurrency & Observer Pattern - Current / Next

| Thành viên | Việc cần làm |
|---|---|
| Huy | `AuctionService` / `BidService`, `ReentrantLock`, `LockRegistry`, `AuctionLockManager`, Observer skeleton |
| Mạnh | `ItemDao`, `AuctionDao`, `BidDao`, SQLite implementations, DAO tests |
| Linh | Chuẩn bị `SocketClient`, `AuthClientService`, nối Login/Register về sau |
| Hải Anh | Chuẩn bị LiveBidding nhận `BidUpdateEvent`, `Platform.runLater()` về sau |

Branch đề xuất:

```text
feature/sync-ui-common-server-contract
feature/sync-domain-model-condition
feature/sqlite-item-auction-bid-dao-manh
feature/auction-locking-bid-service-huy
feature/client-socket-auth-linh
feature/live-bidding-realtime-haianh
```

## W8 - Exception Handling & Unit Testing

| Thành viên | Việc cần làm |
|---|---|
| Huy | AuthService thật, PasswordHasher, SessionManager, AuthController, router LOGIN/REGISTER thật |
| Mạnh | Custom exceptions, AuctionServiceTest, BidServiceTest, DAO tests edge cases |
| Linh | Login/Register gọi socket thật, error state, shared CSS |
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
| Huy | RequestRouter gọi controller/service thật, ClientHandler session-aware, JSON error response chuẩn |
| Mạnh | Checkstyle Maven, GitHub Actions, seed.sql, database polish |
| Linh | SocketClient thật cho Login/Register, xử lý lỗi kết nối |
| Hải Anh | AuctionDetail/LiveBidding gọi server thật, PLACE_BID response update UI |

## W10 - Full realtime & GUI hoàn thiện

| Thành viên | Việc cần làm |
|---|---|
| Huy | BroadcastService, subscribe/unsubscribe auction, thread-safe client registry |
| Mạnh | AuctionScheduler thật, OPEN -> RUNNING -> FINISHED, broadcast AUCTION_CLOSED |
| Linh | AuctionList realtime update, dashboard thật |
| Hải Anh | LiveBidding nhận BID_UPDATE, `Platform.runLater()`, LineChart realtime |

## W11-W12 - Tích hợp toàn bộ & E2E Testing

- Chạy Server + nhiều Client đồng thời.
- Test flow Bidder:

```text
register -> login -> browse auctions -> bid -> receive realtime update -> win/lose result
```

- Test flow Seller:

```text
login -> create item -> create auction -> watch bids -> auction finished -> view result
```

- Fix bug tích hợp.
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

## 16. Immediate action plan (Task Board)

Dưới đây là danh sách các task cần thực hiện ngay. Các thành viên (hoặc Agent) hãy đánh dấu `[x]` khi hoàn thành và ghi rõ branch.

### 16.1 Tầng Dữ liệu & Cấu trúc (Mạnh phụ trách)
- [x] Triển khai `UserDao` & `SQLiteUserDao` (Done - feature/sqlite-userdao-manh)
- [ ] Triển khai `ItemDao` & `SQLiteItemDao` (Pending)
- [ ] Triển khai `AuctionDao` & `SQLiteAuctionDao` (Pending)
- [ ] Triển khai `BidDao` & `SQLiteBidDao` (Pending)
- [ ] Viết Unit Test cho toàn bộ DAO (In Progress)

### 16.2 Tầng Nghiệp vụ & Concurrency (Huy phụ trách)
- [x] Triển khai `AuctionLockManager` (Done - feature/auction-locking-bid-service-huy)
- [x] Triển khai `BidService` xử lý đặt giá (Done - feature/auction-locking-bid-service-huy)
- [ ] Triển khai `AuthService` (Password hashing, Session) (Pending)
- [ ] Triển khai `AuctionService` (Quản lý vòng đời đấu giá) (Pending)

### 16.3 Tầng Giao tiếp & Client (Linh & Hải Anh phụ trách)
- [x] Đồng bộ UI contract với Server Mock (Done - feature/sync-ui-common-server-contract-huy)
- [ ] Triển khai `SocketClient` chạy nền (Pending)
- [ ] Nối màn hình Login/Register với Server thật (Pending)
- [ ] Nối màn hình Live Bidding với Server thật (Pending)
- [ ] Realtime Update UI bằng `Platform.runLater()` (Pending)

---

## 17. Testing checklist trước khi Commit
Hành động bắt buộc cho mọi thành viên:
1. `mvn clean install` -> Pass build.
2. `mvn test` -> Pass all unit tests.
3. Chạy Server và Client đồng thời -> Kiểm tra không lỗi Runtime.

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

Nếu branch sửa socket protocol:

```bash
mvn -pl server exec:java
ncat localhost 8080
```

---

## 18. Coding conventions

- Java class: `PascalCase`.
- Method/variable: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- DAO naming thống nhất dùng `Dao`, không dùng lẫn `DAO`:
  - `UserDao`
  - `SQLiteUserDao`
  - `AuctionDao`
  - `SQLiteAuctionDao`
- Không để magic string cho message type; dùng `MessageType` enum.
- Không để business logic thật trong JavaFX Controller.
- Không để SQL trong Service.
- Không để client import server package.
- Không commit file runtime/build output:

```text
target/
*.class
*.jar
auction.db
*.db
*.sqlite
dependency-reduced-pom.xml
```

---

## 19. Presentation notes

Khi bảo vệ, nhóm cần giải thích được:

### Huy

- Kiến trúc Client-Server.
- Maven multi-module.
- JSON protocol.
- SocketServer / ClientHandler / RequestRouter.
- Singleton / Factory / Observer planned.
- ReentrantLock trong `placeBid()` sau khi hoàn thành W7.

### Mạnh

- SQLite schema.
- DAO layer.
- WAL mode.
- Unit test backend.
- Scheduler planned.
- Exception handling planned.

### Linh

- JavaFX MVC.
- FXML Controller.
- Login/Register.
- AppShell / Dashboard / AuctionList.
- Client service gọi socket sau khi hoàn thành W9.

### Hải Anh

- AuctionDetail.
- LiveBidding.
- Bid history chart.
- Auto-bidding mock.
- Realtime update UI bằng `Platform.runLater()` sau khi hoàn thành Observer.
- Seller screens.

---

## 20. Definition of Done

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

---

## 21. Trạng thái demo hiện tại

Hiện tại nhóm có thể demo:

```text
1. Chạy JavaFX client.
2. Login mock theo username.
3. Vào dashboard theo role.
4. Bidder xem auction list/detail/live bidding mock.
5. Live bidding mock: đặt giá, cập nhật chart, auto-bid giả lập.
6. Seller vào seller center/create auction mock.
7. Admin vào admin panel mock.
8. Chạy socket server.
9. Test ncat LOGIN / PLACE_BID mock.
```

Chưa nên nói demo đã có full backend thật. Cần trình bày đúng là:

```text
UI mock + socket protocol foundation + SQLite foundation + OOP model foundation.
```

Core backend thật sẽ hoàn thành trong các branch W7-W10.
