# Online Auction System - AuctionPro

> Bài tập lớn Lập trình nâng cao 2026: xây dựng hệ thống đấu giá trực tuyến theo kiến trúc **Client-Server**, sử dụng **Java 21**, **JavaFX**, **Socket JSON**, **SQLite**, **Maven multi-module**, **JUnit 5** và **Git/GitHub**.

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

## 2. Thành viên và phân công chính

| Vai trò | Thành viên | Phụ trách chính |
|---|---|---|
| Backend 1 / Lead | Huy | Kiến trúc tổng thể, Maven multi-module, JSON protocol, socket server, auth, auction core, concurrency, review tích hợp |
| Backend 2 | Mạnh | DAO/Repository, SQLite, scheduler, realtime backend, custom exceptions, unit test backend, CI/CD |
| Frontend 1 | Linh | Login/Register, dashboard, auction list, AppShell, layout chung, CSS JavaFX |
| Frontend 2 | Hải Anh | Auction detail, live bidding screen, seller screens, realtime chart, admin UI tối giản |

Lead không có nghĩa là code hết. Lead chịu trách nhiệm khóa kiến trúc, protocol, tiêu chuẩn merge và đảm bảo mọi module tích hợp được.

## 3. Mục tiêu chức năng

### 3.1 Chức năng bắt buộc

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
  - Không có rollback giá.
  - Không có hai người cùng thắng.
- OOP:
  - Encapsulation, Inheritance, Polymorphism, Abstraction.
- Design Patterns:
  - Singleton.
  - Factory Method.
  - Observer.
- Unit test và CI/CD:
  - JUnit cho logic quan trọng.
  - GitHub Actions chạy test khi push / pull request.

### 3.2 Chức năng nâng cao nếu kịp

Ưu tiên theo thứ tự:

1. **Bid History Visualization**: JavaFX LineChart hiển thị giá theo thời gian thực.
2. **Anti-sniping**: nếu có bid trong X giây cuối thì tự động gia hạn thêm Y giây.
3. **Auto-Bidding**: user đặt `maxBid`, `increment`, hệ thống tự tăng giá thay user.

## 4. Kiến trúc tổng thể

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
- Các DTO, enum, model dùng chung đặt trong module `common`.
- UI JavaFX tách theo MVC: FXML là View, Controller xử lý UI, Service client gọi socket.
- Server tách theo Controller -> Service -> DAO.

## 5. Cấu trúc Maven multi-module

```text
online-auction-system/
├── README.md
├── pom.xml
├── .gitignore
├── .editorconfig
├── checkstyle.xml
├── LICENSE
│
├── .github/
│   └── workflows/
│       └── maven.yml
│
├── docs/
│   ├── protocol.md
│   ├── architecture.md
│   ├── database-schema.md
│   ├── class-diagram.md
│   ├── git-workflow.md
│   ├── test-plan.md
│   ├── demo-script.md
│   └── ui-design.md
│
├── common/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   └── java/
│       │       └── com/auction/common/
│       │           ├── enums/
│       │           │   ├── Role.java
│       │           │   ├── AuctionStatus.java
│       │           │   ├── ItemType.java
│       │           │   ├── ResponseStatus.java
│       │           │   └── BidType.java
│       │           │
│       │           ├── protocol/
│       │           │   ├── MessageType.java
│       │           │   ├── Request.java
│       │           │   ├── Response.java
│       │           │   └── ErrorResponse.java
│       │           │
│       │           ├── dto/
│       │           │   ├── auth/
│       │           │   │   ├── LoginRequest.java
│       │           │   │   ├── LoginResponse.java
│       │           │   │   ├── RegisterRequest.java
│       │           │   │   └── RegisterResponse.java
│       │           │   │
│       │           │   ├── user/
│       │           │   │   ├── UserDto.java
│       │           │   │   ├── UserSummaryDto.java
│       │           │   │   └── UpdateUserStatusRequest.java
│       │           │   │
│       │           │   ├── item/
│       │           │   │   ├── ItemDto.java
│       │           │   │   ├── CreateItemRequest.java
│       │           │   │   ├── UpdateItemRequest.java
│       │           │   │   └── ItemSummaryDto.java
│       │           │   │
│       │           │   ├── auction/
│       │           │   │   ├── AuctionDto.java
│       │           │   │   ├── AuctionSummaryDto.java
│       │           │   │   ├── CreateAuctionRequest.java
│       │           │   │   ├── UpdateAuctionRequest.java
│       │           │   │   ├── AuctionDetailResponse.java
│       │           │   │   ├── AuctionListRequest.java
│       │           │   │   ├── AuctionListResponse.java
│       │           │   │   ├── AuctionClosedEvent.java
│       │           │   │   └── TimeExtendedEvent.java
│       │           │   │
│       │           │   ├── bid/
│       │           │   │   ├── BidDto.java
│       │           │   │   ├── PlaceBidRequest.java
│       │           │   │   ├── PlaceBidResponse.java
│       │           │   │   ├── BidHistoryRequest.java
│       │           │   │   ├── BidHistoryResponse.java
│       │           │   │   ├── BidUpdateEvent.java
│       │           │   │   ├── AutoBidRequest.java
│       │           │   │   └── AutoBidDto.java
│       │           │   │
│       │           │   └── dashboard/
│       │           │       ├── DashboardSummaryDto.java
│       │           │       ├── MarketFeedDto.java
│       │           │       └── StatCardDto.java
│       │           │
│       │           ├── model/
│       │           │   ├── Entity.java
│       │           │   ├── User.java
│       │           │   ├── Bidder.java
│       │           │   ├── Seller.java
│       │           │   ├── Admin.java
│       │           │   ├── Item.java
│       │           │   ├── Electronics.java
│       │           │   ├── Art.java
│       │           │   ├── Vehicle.java
│       │           │   ├── Auction.java
│       │           │   ├── BidTransaction.java
│       │           │   └── AutoBidRule.java
│       │           │
│       │           ├── exception/
│       │           │   ├── AuctionException.java
│       │           │   ├── InvalidBidException.java
│       │           │   ├── AuctionClosedException.java
│       │           │   ├── AuthenticationException.java
│       │           │   ├── AuthorizationException.java
│       │           │   ├── NotFoundException.java
│       │           │   ├── ValidationException.java
│       │           │   └── DataAccessException.java
│       │           │
│       │           └── util/
│       │               ├── MoneyUtils.java
│       │               ├── DateTimeUtils.java
│       │               └── ValidationUtils.java
│       │
│       └── test/
│           └── java/com/auction/common/
│               ├── protocol/
│               │   └── RequestResponseTest.java
│               ├── model/
│               │   ├── UserModelTest.java
│               │   ├── ItemModelTest.java
│               │   └── AuctionModelTest.java
│               └── util/
│                   └── ValidationUtilsTest.java
│
├── server/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/auction/server/
│       │   │       ├── ServerMain.java
│       │   │       │
│       │   │       ├── socket/
│       │   │       │   ├── SocketServer.java
│       │   │       │   ├── ClientHandler.java
│       │   │       │   ├── ClientSession.java
│       │   │       │   ├── ClientConnectionRegistry.java
│       │   │       │   └── RequestRouter.java
│       │   │       │
│       │   │       ├── controller/
│       │   │       │   ├── AuthController.java
│       │   │       │   ├── UserController.java
│       │   │       │   ├── ItemController.java
│       │   │       │   ├── AuctionController.java
│       │   │       │   ├── BidController.java
│       │   │       │   ├── AdminController.java
│       │   │       │   └── DashboardController.java
│       │   │       │
│       │   │       ├── service/
│       │   │       │   ├── AuthService.java
│       │   │       │   ├── UserService.java
│       │   │       │   ├── ItemService.java
│       │   │       │   ├── AuctionService.java
│       │   │       │   ├── BidService.java
│       │   │       │   ├── AutoBidService.java
│       │   │       │   ├── BroadcastService.java
│       │   │       │   └── DashboardService.java
│       │   │       │
│       │   │       ├── dao/
│       │   │       │   ├── Database.java
│       │   │       │   ├── TransactionManager.java
│       │   │       │   ├── UserDao.java
│       │   │       │   ├── ItemDao.java
│       │   │       │   ├── AuctionDao.java
│       │   │       │   ├── BidDao.java
│       │   │       │   ├── AutoBidDao.java
│       │   │       │   └── sqlite/
│       │   │       │       ├── SQLiteUserDao.java
│       │   │       │       ├── SQLiteItemDao.java
│       │   │       │       ├── SQLiteAuctionDao.java
│       │   │       │       ├── SQLiteBidDao.java
│       │   │       │       └── SQLiteAutoBidDao.java
│       │   │       │
│       │   │       ├── mapper/
│       │   │       │   ├── UserMapper.java
│       │   │       │   ├── ItemMapper.java
│       │   │       │   ├── AuctionMapper.java
│       │   │       │   └── BidMapper.java
│       │   │       │
│       │   │       ├── factory/
│       │   │       │   └── ItemFactory.java
│       │   │       │
│       │   │       ├── observer/
│       │   │       │   ├── AuctionObserver.java
│       │   │       │   ├── AuctionSubject.java
│       │   │       │   └── AuctionEventType.java
│       │   │       │
│       │   │       ├── scheduler/
│       │   │       │   └── AuctionScheduler.java
│       │   │       │
│       │   │       ├── security/
│       │   │       │   ├── PasswordHasher.java
│       │   │       │   ├── SessionManager.java
│       │   │       │   ├── SessionToken.java
│       │   │       │   └── PermissionChecker.java
│       │   │       │
│       │   │       ├── concurrency/
│       │   │       │   ├── LockRegistry.java
│       │   │       │   └── AuctionLockManager.java
│       │   │       │
│       │   │       ├── config/
│       │   │       │   ├── ServerConfig.java
│       │   │       │   └── AppProperties.java
│       │   │       │
│       │   │       └── util/
│       │   │           ├── JsonMapper.java
│       │   │           ├── ResponseFactory.java
│       │   │           ├── MoneyFormatter.java
│       │   │           └── LoggerUtil.java
│       │   │
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/
│       │           ├── schema.sql
│       │           ├── seed.sql
│       │           └── test-seed.sql
│       │
│       └── test/
│           └── java/com/auction/server/
│               ├── service/
│               │   ├── AuthServiceTest.java
│               │   ├── AuctionServiceTest.java
│               │   ├── BidServiceTest.java
│               │   ├── AutoBidServiceTest.java
│               │   └── DashboardServiceTest.java
│               ├── dao/
│               │   ├── SQLiteUserDaoTest.java
│               │   ├── SQLiteItemDaoTest.java
│               │   ├── SQLiteAuctionDaoTest.java
│               │   └── SQLiteBidDaoTest.java
│               ├── scheduler/
│               │   └── AuctionSchedulerTest.java
│               ├── concurrency/
│               │   └── ConcurrentBiddingTest.java
│               ├── socket/
│               │   └── RequestRouterTest.java
│               └── security/
│                   ├── PasswordHasherTest.java
│                   └── SessionManagerTest.java
│
└── client/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/auction/client/
        │   │       ├── ClientMain.java
        │   │       │
        │   │       ├── app/
        │   │       │   ├── AppContext.java
        │   │       │   ├── UserSession.java
        │   │       │   └── ClientConfig.java
        │   │       │
        │   │       ├── network/
        │   │       │   ├── SocketClient.java
        │   │       │   ├── MessageListener.java
        │   │       │   ├── ServerEventDispatcher.java
        │   │       │   ├── PendingRequestRegistry.java
        │   │       │   └── ConnectionState.java
        │   │       │
        │   │       ├── controller/
        │   │       │   ├── LoginController.java
        │   │       │   ├── RegisterController.java
        │   │       │   ├── AppShellController.java
        │   │       │   ├── SidebarController.java
        │   │       │   ├── TopBarController.java
        │   │       │   ├── DashboardController.java
        │   │       │   ├── AuctionListController.java
        │   │       │   ├── AuctionDetailController.java
        │   │       │   ├── LiveBiddingController.java
        │   │       │   ├── MyBidsController.java
        │   │       │   ├── SellerCenterController.java
        │   │       │   ├── CreateAuctionController.java
        │   │       │   ├── EditAuctionController.java
        │   │       │   └── AdminPanelController.java
        │   │       │
        │   │       ├── viewmodel/
        │   │       │   ├── AuctionCardViewModel.java
        │   │       │   ├── AuctionDetailViewModel.java
        │   │       │   ├── BidHistoryViewModel.java
        │   │       │   ├── SellerListingViewModel.java
        │   │       │   ├── AdminUserViewModel.java
        │   │       │   ├── AdminAuctionViewModel.java
        │   │       │   ├── DashboardStatViewModel.java
        │   │       │   └── MarketFeedViewModel.java
        │   │       │
        │   │       ├── service/
        │   │       │   ├── ClientAuthService.java
        │   │       │   ├── ClientAuctionService.java
        │   │       │   ├── ClientBidService.java
        │   │       │   ├── ClientSellerService.java
        │   │       │   ├── ClientAdminService.java
        │   │       │   └── MockDataService.java
        │   │       │
        │   │       ├── component/
        │   │       │   ├── AuctionCard.java
        │   │       │   ├── StatCard.java
        │   │       │   ├── StatusBadge.java
        │   │       │   ├── Toast.java
        │   │       │   └── ConfirmDialog.java
        │   │       │
        │   │       └── util/
        │   │           ├── SceneManager.java
        │   │           ├── FxmlLoader.java
        │   │           ├── AlertUtil.java
        │   │           ├── FormatUtil.java
        │   │           ├── TimeFormatter.java
        │   │           └── ImageUtil.java
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
        │       │   ├── MyBidsView.fxml
        │       │   ├── SellerCenterView.fxml
        │       │   ├── CreateAuctionView.fxml
        │       │   ├── EditAuctionView.fxml
        │       │   ├── AdminPanelView.fxml
        │       │   └── components/
        │       │       ├── Sidebar.fxml
        │       │       ├── TopBar.fxml
        │       │       ├── AuctionCard.fxml
        │       │       ├── StatCard.fxml
        │       │       ├── StatusBadge.fxml
        │       │       ├── BidHistoryTable.fxml
        │       │       └── EmptyState.fxml
        │       │
        │       ├── css/
        │       │   ├── app.css
        │       │   ├── login.css
        │       │   ├── dashboard.css
        │       │   ├── auction.css
        │       │   └── table.css
        │       │
        │       ├── images/
        │       │   ├── logo.png
        │       │   ├── placeholder-item.png
        │       │   ├── avatar-placeholder.png
        │       │   ├── sample-watch.png
        │       │   ├── sample-car.png
        │       │   ├── sample-art.png
        │       │   ├── sample-camera.png
        │       │   └── sample-guitar.png
        │       │
        │       └── fonts/
        │           └── README.md
        │
        └── test/
            └── java/com/auction/client/
                ├── viewmodel/
                │   ├── AuctionCardViewModelTest.java
                │   └── BidHistoryViewModelTest.java
                ├── util/
                │   ├── FormatUtilTest.java
                │   └── TimeFormatterTest.java
                └── service/
                    └── MockDataServiceTest.java
```

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
// Sai
import com.auction.server.service.AuthService; // trong client
import com.auction.server.dao.UserDao;         // trong client
import com.auction.client.controller.LoginController; // trong server
```

Client chỉ gọi server qua:

```java
SocketClient.send(Request<?> request)
```

## 7. JSON protocol

### 7.1 Request format

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

### 7.2 Response format

```json
{
  "type": "PLACE_BID_RESULT",
  "requestId": "uuid-123",
  "success": true,
  "message": "Bid accepted",
  "data": {
    "auctionId": 1,
    "currentPrice": 1500000,
    "highestBidder": "huy"
  }
}
```

### 7.3 Realtime event format

```json
{
  "type": "BID_UPDATE",
  "data": {
    "auctionId": 1,
    "amount": 1500000,
    "bidderUsername": "huy",
    "timestamp": "2026-05-04T20:30:00",
    "newEndTime": null
  }
}
```

### 7.4 Message types tối thiểu

```text
REGISTER
LOGIN
LOGOUT

GET_DASHBOARD_SUMMARY
GET_MARKET_FEED

GET_AUCTIONS
GET_AUCTION_DETAIL
CREATE_ITEM
UPDATE_ITEM
DELETE_ITEM
CREATE_AUCTION
UPDATE_AUCTION
CANCEL_AUCTION

PLACE_BID
GET_BID_HISTORY
SUBSCRIBE_AUCTION
UNSUBSCRIBE_AUCTION

BID_UPDATE
AUCTION_CLOSED
TIME_EXTENDED

ADMIN_GET_USERS
ADMIN_LOCK_USER
ADMIN_UNLOCK_USER
ADMIN_GET_AUCTIONS
ADMIN_CANCEL_AUCTION
```

## 8. Database SQLite

File chính:

```text
server/src/main/resources/db/schema.sql
server/src/main/resources/db/seed.sql
server/src/main/resources/db/test-seed.sql
```

### 8.1 `users`

```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT,
    role TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL
);
```

### 8.2 `items`

```sql
CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    seller_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    starting_price REAL NOT NULL,
    image_path TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id)
);
```

### 8.3 `auctions`

```sql
CREATE TABLE IF NOT EXISTS auctions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    seller_id INTEGER NOT NULL,
    current_price REAL NOT NULL,
    highest_bidder_id INTEGER,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    status TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
);
```

### 8.4 `bids`

```sql
CREATE TABLE IF NOT EXISTS bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);
```

### 8.5 `auto_bids`

```sql
CREATE TABLE IF NOT EXISTS auto_bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    max_bid REAL NOT NULL,
    increment REAL NOT NULL,
    created_at TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);
```

### 8.6 SQLite PRAGMA bắt buộc

Khi mở connection, bật:

```sql
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA busy_timeout = 5000;
```

Lưu ý: WAL giúp giảm lỗi `SQLITE_BUSY`, nhưng không thay thế lock nghiệp vụ trong `BidService.placeBid()`.

## 9. Auction state machine

```text
OPEN -> RUNNING -> FINISHED -> PAID
                    |
                    v
                 CANCELED
```

Ý nghĩa:

| Status | Ý nghĩa |
|---|---|
| `OPEN` | Phiên đã tạo nhưng chưa đến giờ bắt đầu |
| `RUNNING` | Đang diễn ra và nhận bid |
| `FINISHED` | Đã hết giờ, đã xác định winner nếu có |
| `PAID` | Người thắng đã thanh toán / seller xác nhận |
| `CANCELED` | Bị hủy bởi seller/admin hoặc lỗi dữ liệu |

`AuctionScheduler` chịu trách nhiệm:

- Chuyển `OPEN -> RUNNING` khi đến `startTime`.
- Chuyển `RUNNING -> FINISHED` khi quá `endTime`.
- Xác định winner từ `highestBidderId`.
- Broadcast `AUCTION_CLOSED` tới client đang subscribe auction.

## 10. Logic đặt giá `placeBid()`

`BidService.placeBid()` là phần quan trọng nhất của backend.

Pseudo-code:

```java
public PlaceBidResponse placeBid(long auctionId, long bidderId, BigDecimal amount) {
    ReentrantLock lock = lockRegistry.getLock(auctionId);
    lock.lock();

    try {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new NotFoundException("Auction not found"));

        LocalDateTime now = LocalDateTime.now();

        if (!auction.isRunningAt(now)) {
            throw new AuctionClosedException("Auction is not running");
        }

        if (auction.getSellerId() == bidderId) {
            throw new InvalidBidException("Seller cannot bid on own auction");
        }

        if (amount.compareTo(auction.getCurrentPrice()) <= 0) {
            throw new InvalidBidException("Bid must be higher than current price");
        }

        BidTransaction bid = new BidTransaction(auctionId, bidderId, amount, now);
        bidDao.insert(bid);

        auction.setCurrentPrice(amount);
        auction.setHighestBidderId(bidderId);

        boolean extended = antiSnipingIfNeeded(auction, now);

        auctionDao.updateAfterBid(auction);

        PlaceBidResponse response = PlaceBidResponse.accepted(auction, bid, extended);
        broadcastService.broadcastBidUpdate(response);

        return response;
    } finally {
        lock.unlock();
    }
}
```

Điểm cần nhớ khi bảo vệ:

- Lock theo `auctionId`, không lock toàn server.
- Check giá và update giá nằm trong cùng critical section.
- Insert bid và update auction nên nằm trong transaction.
- Broadcast sau khi database update thành công.
- Client không tự quyết định winner.

## 11. Design Patterns

| Pattern | Vị trí triển khai | Mục đích |
|---|---|---|
| Singleton | `Database`, `SessionManager`, có thể `BroadcastService` | Quản lý tài nguyên dùng chung |
| Factory Method | `ItemFactory` | Tạo `Electronics`, `Art`, `Vehicle` theo `ItemType` |
| Observer | `BroadcastService`, `AuctionSubject`, client subscribe auction | Realtime bid update |
| Strategy | `ManualBidStrategy`, `AutoBidStrategy` nếu làm auto-bidding | Tách logic bid thường và bid tự động |
| Command | `BidCommand` nếu muốn đóng gói thao tác bid | Tùy chọn, không bắt buộc |

## 12. UI / JavaFX design

UI tham khảo từ Google Stitch, chuyển sang JavaFX FXML + CSS.

### 12.1 Màn hình cần có

| Màn hình | File FXML | Người chính |
|---|---|---|
| Login | `LoginView.fxml` | Linh |
| Register | `RegisterView.fxml` | Linh |
| App shell | `AppShell.fxml`, `Sidebar.fxml`, `TopBar.fxml` | Linh |
| Dashboard | `DashboardView.fxml` | Linh |
| Auction list | `AuctionListView.fxml` | Linh |
| Auction detail | `AuctionDetailView.fxml` | Hải Anh |
| Live bidding | `LiveBiddingView.fxml` | Hải Anh |
| My bids | `MyBidsView.fxml` | Linh / Hải Anh |
| Seller center | `SellerCenterView.fxml` | Hải Anh |
| Create auction | `CreateAuctionView.fxml` | Hải Anh |
| Edit auction | `EditAuctionView.fxml` | Hải Anh |
| Admin panel | `AdminPanelView.fxml` | Hải Anh |

### 12.2 CSS chính

File:

```text
client/src/main/resources/css/app.css
```

Design token chính:

```css
.root {
    -fx-font-family: "Inter", "Segoe UI", "Arial";
    -fx-background-color: #fcf8ff;

    -color-primary: #3525cd;
    -color-primary-container: #4f46e5;
    -color-on-primary: #ffffff;

    -color-surface: #fcf8ff;
    -color-surface-lowest: #ffffff;
    -color-surface-low: #f5f2ff;
    -color-surface-container: #f0ecf9;
    -color-outline: #777587;
    -color-outline-variant: #c7c4d8;

    -color-text: #1b1b24;
    -color-text-muted: #464555;

    -color-error: #ba1a1a;
    -color-error-container: #ffdad6;
    -color-success: #059669;
}
```

Component class nên dùng:

```css
.sidebar {
    -fx-background-color: #312e81;
    -fx-pref-width: 256px;
}

.nav-button {
    -fx-background-color: transparent;
    -fx-text-fill: #c7d2fe;
    -fx-font-size: 14px;
    -fx-alignment: CENTER_LEFT;
    -fx-padding: 12 16 12 16;
    -fx-cursor: hand;
}

.nav-button-active {
    -fx-background-color: #3730a3;
    -fx-text-fill: white;
    -fx-border-color: transparent transparent transparent #818cf8;
    -fx-border-width: 0 0 0 4;
}

.card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-border-color: #c7c4d8;
    -fx-border-radius: 8;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 4, 0, 0, 1);
}

.primary-button {
    -fx-background-color: #3525cd;
    -fx-text-fill: white;
    -fx-font-weight: 600;
    -fx-background-radius: 8;
    -fx-padding: 10 18 10 18;
    -fx-cursor: hand;
}

.secondary-button {
    -fx-background-color: transparent;
    -fx-text-fill: #3525cd;
    -fx-border-color: #c7c4d8;
    -fx-border-radius: 8;
    -fx-background-radius: 8;
    -fx-padding: 10 18 10 18;
    -fx-cursor: hand;
}

.money {
    -fx-font-family: "Consolas", "JetBrains Mono", monospace;
    -fx-font-weight: 700;
}
```

### 12.3 Realtime UI update

Mọi update UI từ socket thread phải dùng:

```java
Platform.runLater(() -> {
    currentPriceLabel.setText(formatMoney(event.amount()));
    highestBidderLabel.setText(event.bidderUsername());
    bidHistory.add(0, event.toBidDto());
    priceSeries.getData().add(new XYChart.Data<>(formatTime(event.timestamp()), event.amount()));
});
```

Không update JavaFX UI trực tiếp từ background thread.

## 13. Lộ trình theo tuần

### W6 - Khởi động và thiết kế OOP

| Người | Việc |
|---|---|
| Huy | Tạo Maven multi-module, parent pom, `common/server/client`, protocol base, branch `main/dev` |
| Mạnh | Tạo schema SQLite, `Database`, DAO interface |
| Linh | Setup JavaFX, `LoginView`, `RegisterView`, `AppShell`, `Sidebar`, `TopBar` |
| Hải Anh | Tạo mock `AuctionDetail`, `LiveBidding`, `SellerCenter`, `CreateAuction` |

Deliverable:

```text
mvn clean test chạy xanh
ServerMain chạy được
ClientMain mở được JavaFX window
docs/protocol.md bản đầu
docs/database-schema.md bản đầu
```

### W7 - Concurrency và Observer Pattern

| Người | Việc |
|---|---|
| Huy | `SocketServer`, `ClientHandler`, `RequestRouter`, `BidService.placeBid()` với `ReentrantLock` |
| Mạnh | SQLite DAO cho `User`, `Item`, `Auction`, `Bid` |
| Linh | Login/Register mock controller, Dashboard/AuctionList mock |
| Hải Anh | LiveBidding mock data, bid history table, chart mock |

Deliverable:

```text
Tạo auction được bằng service
Đặt bid hợp lệ cập nhật currentPrice
Bid thấp bị từ chối
Observer skeleton gọi được listener mock
```

### W8 - Exception Handling và Unit Testing

| Người | Việc |
|---|---|
| Huy | AuthService, JSON serialization, error response chuẩn |
| Mạnh | Custom exceptions, JUnit, JaCoCo, DAO tests |
| Linh | Register validation, shared layout, CSS |
| Hải Anh | Seller dashboard, Add/Edit item form |

Deliverable:

```text
Ít nhất 10 unit tests
Test bid hợp lệ / không hợp lệ
Test auction closed
Test state transition
Coverage ban đầu >= 50%, sau nâng lên >= 60%
```

### W9 - CI/CD và Socket Integration

| Người | Việc |
|---|---|
| Huy | Tích hợp SocketServer với AuthService/AuctionService thật |
| Mạnh | GitHub Actions, Checkstyle, init schema tự động, scheduler |
| Linh | Login/Register gọi SocketClient thật |
| Hải Anh | AuctionDetail gọi server lấy data thật |

Deliverable:

```text
Login/Register qua socket thật
GET_AUCTIONS trả danh sách thật từ SQLite
GitHub Actions chạy mvn test khi push/PR
```

### W10 - Full Realtime và GUI hoàn thiện

| Người | Việc |
|---|---|
| Huy | BroadcastService thread-safe, SUBSCRIBE_AUCTION, BID_UPDATE |
| Mạnh | WAL mode, AUCTION_CLOSED event, Scheduler ổn định |
| Linh | AuctionList nhận update qua `Platform.runLater()` |
| Hải Anh | LiveBidding nhận `BID_UPDATE`, LineChart thêm point mới |

Deliverable:

```text
Mở 2 client cùng xem 1 auction
Client A bid
Client B thấy currentPrice đổi ngay
Bid history table cập nhật
LineChart cập nhật
```

### W11-W12 - Tích hợp toàn bộ và E2E Testing

| Người | Việc |
|---|---|
| Huy | E2E test 3-4 client, fix race condition, review toàn bộ code |
| Mạnh | Fix DB edge cases, restart server không mất data, CI/CD pass |
| Linh | Test Bidder flow: register -> login -> browse -> bid -> result |
| Hải Anh | Test Seller flow: create item -> create auction -> receive bids -> close |

Deliverable:

```text
Server + 3 client chạy ổn
Seller tạo auction thật
Bidder đặt giá thật
Auction tự đóng
Winner hiển thị đúng
```

### W13-W14 - Polish và chức năng nâng cao

| Người | Việc |
|---|---|
| Huy | Anti-sniping core hoặc Auto-bidding core |
| Mạnh | Tối ưu SQLite, thêm index, full test suite |
| Linh | Polish UI, loading state, error state, README |
| Hải Anh | LineChart realtime hoàn chỉnh, Anti-sniping UI, demo video backup |

Deliverable:

```text
Bid History LineChart realtime
Anti-sniping nếu có bid trong X giây cuối thì extend endTime
Release v1.0
README đầy đủ
Slide/demo script xong
```

### W15 - Trình bày và chấm điểm

| Người | Trình bày |
|---|---|
| Huy | Architecture, protocol, class diagram, design patterns, concurrency |
| Mạnh | DAO, SQLite, scheduler, unit test, CI/CD |
| Linh | Bidder flow, JavaFX MVC, FXML binding |
| Hải Anh | Seller flow, realtime chart, Observer client, optional features |

## 14. Git workflow

### 14.1 Branch

```text
main       code ổn định để demo / nộp
dev        nhánh tích hợp hằng ngày
feature/*  nhánh làm từng task
```

Ví dụ:

```text
feature/project-skeleton
feature/socket-server-huy
feature/sqlite-dao-manh
feature/login-ui-linh
feature/live-bidding-ui-haianh
```

### 14.2 Quy tắc merge

- Không push thẳng vào `main`.
- Không push thẳng vào `dev` nếu task lớn.
- Mỗi task tạo Pull Request vào `dev`.
- Huy review phần kiến trúc/protocol/concurrency.
- Mạnh review phần DAO/test nếu liên quan backend.
- Linh và Hải Anh review chéo UI để cả nhóm hiểu code.
- Cuối mỗi tuần, nếu `dev` ổn định thì merge `dev -> main`.

### 14.3 Conventional Commits

```text
feat: add login request handler
feat: implement auction list screen
fix: prevent bid lower than current price
test: add concurrent bidding test
docs: define socket json protocol
refactor: split auction service and bid service
style: update JavaFX CSS for auction cards
chore: initialize multi-module maven project
```

## 15. Backlog GitHub Issues

### Epic 1 - Project setup

```text
SETUP-01: Create parent Maven project
SETUP-02: Create common/server/client modules
SETUP-03: Configure Java 21 compiler
SETUP-04: Add JavaFX dependencies
SETUP-05: Add Gson/Jackson dependency
SETUP-06: Add SQLite JDBC dependency
SETUP-07: Add JUnit 5 dependency
SETUP-08: Add GitHub Actions workflow
SETUP-09: Add Checkstyle config
```

### Epic 2 - Common protocol

```text
PROTO-01: Define MessageType enum
PROTO-02: Define Request/Response wrapper
PROTO-03: Define auth DTOs
PROTO-04: Define item DTOs
PROTO-05: Define auction DTOs
PROTO-06: Define bid DTOs and realtime events
PROTO-07: Write docs/protocol.md
PROTO-08: Add JSON serialization tests
```

### Epic 3 - Backend server

```text
BE-01: Implement SocketServer
BE-02: Implement ClientHandler
BE-03: Implement RequestRouter
BE-04: Implement SessionManager
BE-05: Implement AuthController
BE-06: Implement AuctionController
BE-07: Implement ItemController
BE-08: Implement BidController
BE-09: Implement AdminController
```

### Epic 4 - Database and DAO

```text
DAO-01: Create SQLite schema
DAO-02: Implement Database singleton
DAO-03: Enable WAL and busy_timeout
DAO-04: Implement SQLiteUserDao
DAO-05: Implement SQLiteItemDao
DAO-06: Implement SQLiteAuctionDao
DAO-07: Implement SQLiteBidDao
DAO-08: Add seed data
```

### Epic 5 - Auction logic

```text
BID-01: Implement AuctionService.createAuction
BID-02: Implement AuctionService.getRunningAuctions
BID-03: Implement BidService.placeBid
BID-04: Add ReentrantLock per auctionId
BID-05: Prevent seller bidding on own auction
BID-06: Prevent bid on closed auction
BID-07: Prevent bid lower than current price
BID-08: Implement winner calculation
BID-09: Implement AuctionScheduler
```

### Epic 6 - Realtime

```text
RT-01: Implement SUBSCRIBE_AUCTION
RT-02: Implement UNSUBSCRIBE_AUCTION
RT-03: Implement BroadcastService
RT-04: Broadcast BID_UPDATE
RT-05: Broadcast AUCTION_CLOSED
RT-06: Broadcast TIME_EXTENDED
RT-07: Implement Client MessageListener
RT-08: JavaFX Platform.runLater integration
```

### Epic 7 - Frontend

```text
FE-01: ClientMain and SceneManager
FE-02: LoginView.fxml
FE-03: RegisterView.fxml
FE-04: AppShell.fxml with Sidebar and TopBar
FE-05: DashboardView.fxml
FE-06: AuctionListView.fxml
FE-07: AuctionDetailView.fxml
FE-08: LiveBiddingView.fxml
FE-09: SellerCenterView.fxml
FE-10: CreateAuctionView.fxml
FE-11: AdminPanelView.fxml
FE-12: app.css and screen-specific CSS
```

### Epic 8 - Testing

```text
TEST-01: AuthServiceTest
TEST-02: BidService valid bid test
TEST-03: BidService invalid low bid test
TEST-04: BidService closed auction test
TEST-05: Concurrent bid test
TEST-06: AuctionSchedulerTest
TEST-07: DAO integration tests
TEST-08: JSON protocol test
```

### Epic 9 - Optional features

```text
OPT-01: Bid History LineChart
OPT-02: Anti-sniping backend
OPT-03: Anti-sniping TIME_EXTENDED event
OPT-04: AutoBidRule model
OPT-05: AutoBidService
```

## 16. Setup môi trường

### 16.1 Yêu cầu

- JDK 21.
- Maven 3.9+.
- Git.
- IntelliJ IDEA hoặc VS Code.
- SceneBuilder nếu dùng thiết kế FXML trực quan.

Kiểm tra:

```bash
java -version
mvn -version
git --version
```

### 16.2 Clone project

```bash
git clone https://github.com/cecon123/online-auction-system.git
cd online-auction-system
```

### 16.3 Build toàn bộ

```bash
mvn clean test
```

### 16.4 Package toàn bộ

```bash
mvn clean package
```

### 16.5 Chạy server

Sau khi package:

```bash
java -jar server/target/auction-server.jar
```

Hoặc chạy bằng Maven nếu cấu hình plugin:

```bash
mvn -pl server exec:java
```

### 16.6 Chạy client JavaFX

```bash
mvn -pl client javafx:run
```

Nếu client cần build kèm common:

```bash
mvn -pl client -am javafx:run
```

### 16.7 Chạy test từng module

```bash
mvn -pl common test
mvn -pl server test
mvn -pl client test
```

Chạy client + build module phụ thuộc:

```bash
mvn -pl client -am clean package
```

## 17. GitHub Actions

File:

```text
.github/workflows/maven.yml
```

Nội dung cơ bản:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ "main", "dev" ]
  pull_request:
    branches: [ "main", "dev" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: maven

      - name: Build and test all modules
        run: mvn clean test
```

Nếu JavaFX lỗi trên CI do môi trường headless, tạm thời chỉ test backend:

```yaml
- name: Test backend modules
  run: mvn -pl common,server test
```

## 18. Test plan

### 18.1 Unit tests bắt buộc

```text
AuthServiceTest
- registerSuccess
- duplicateUsernameShouldFail
- loginSuccess
- wrongPasswordShouldFail

BidServiceTest
- placeValidBidShouldUpdateCurrentPrice
- bidLowerThanCurrentPriceShouldFail
- bidOnClosedAuctionShouldFail
- sellerCannotBidOnOwnAuction
- highestBidderShouldBeUpdated
- concurrentBidsShouldKeepHighestValidBidOnly

AuctionSchedulerTest
- openAuctionShouldBecomeRunning
- expiredAuctionShouldBecomeFinished
- winnerShouldBeHighestBidder

DaoTest
- insertUserAndFindByUsername
- insertItemAndFindBySeller
- insertAuctionAndFindRunning
- insertBidAndFindByAuction
```

### 18.2 E2E demo test thủ công

1. Start server.
2. Mở Client 1: Seller.
3. Mở Client 2: Bidder A.
4. Mở Client 3: Bidder B.
5. Seller tạo item và auction.
6. Bidder A mở auction detail.
7. Bidder B mở cùng auction detail.
8. Bidder A bid 1,000,000.
9. Bidder B thấy realtime update.
10. Bidder B bid 1,200,000.
11. Bidder A thấy realtime update.
12. Bidder A thử bid 1,100,000 -> bị từ chối.
13. Đợi hết giờ hoặc dùng auction endTime ngắn.
14. Server tự đóng phiên.
15. UI hiển thị winner.
16. LineChart hiển thị lịch sử giá.

## 19. Definition of Done

Một task chỉ được coi là xong khi:

- Code build được.
- Không làm fail `mvn test`.
- Có xử lý lỗi cơ bản.
- Có commit rõ ràng.
- Có Pull Request hoặc được review.
- Có cập nhật docs nếu thay đổi protocol/database.
- Người khác trong nhóm đọc và giải thích được flow chính.

## 20. Checklist chấm điểm

| Hạng mục | Cách chứng minh |
|---|---|
| Thiết kế lớp và cây kế thừa | Class diagram, `User -> Bidder/Seller/Admin`, `Item -> Electronics/Art/Vehicle` |
| OOP | Encapsulation, inheritance, polymorphism, abstraction trong model/service |
| Design Patterns | `Database` Singleton, `ItemFactory`, `BroadcastService`/Observer |
| Quản lý user/product | Demo register/login, seller CRUD item/auction |
| Chức năng đấu giá | Demo place bid, current price update, highest bidder |
| Exception | Demo bid thấp, auction đóng, sai login |
| Concurrency | JUnit concurrent bid test + giải thích ReentrantLock |
| Realtime | 2 client xem cùng auction, một client bid client kia update ngay |
| Client-Server | Chạy `ServerMain` và nhiều `ClientMain` riêng biệt |
| MVC | JavaFX FXML + Controller, server Controller-Service-DAO |
| Maven | Multi-module build bằng `mvn clean test` |
| Unit Test | JUnit report |
| CI/CD | GitHub Actions xanh |
| Optional | LineChart, Anti-sniping, Auto-bidding |

## 21. Rủi ro và cách tránh

| Rủi ro | Cách tránh |
|---|---|
| GUI chờ server, server chờ DAO | Khóa protocol JSON từ W6, frontend dùng mock trước |
| JSON mỗi người viết một kiểu | Tất cả request/response phải đi qua DTO trong `common` |
| Race condition khi bid | Chỉ `BidService.placeBid()` được update giá, dùng `ReentrantLock` theo `auctionId` |
| SQLite bị lock | Bật WAL, busy_timeout, transaction rõ ràng |
| JavaFX lỗi thread | Mọi update UI từ socket dùng `Platform.runLater()` |
| Ít commit | Mỗi người commit đều mỗi ngày hoặc mỗi task nhỏ |
| Cuối kỳ không ai hiểu code nhau | Review PR, walkthrough mỗi tuần |
| Demo lỗi mạng | Chuẩn bị seed data và video demo backup |

## 22. Việc cần làm ngay

### Huy

```text
- Tạo parent pom.xml
- Tạo common/server/client module
- Tạo Request, Response, MessageType
- Tạo docs/protocol.md
- Tạo ServerMain skeleton
```

### Mạnh

```text
- Tạo schema.sql
- Tạo Database.java
- Tạo DAO interfaces
- Bật SQLite WAL mode
```

### Linh

```text
- Tạo app.css
- Tạo LoginView.fxml
- Tạo RegisterView.fxml
- Tạo AppShell.fxml
- Tạo Sidebar.fxml và TopBar.fxml
```

### Hải Anh

```text
- Tạo AuctionDetailView.fxml
- Tạo LiveBiddingView.fxml
- Tạo SellerCenterView.fxml
- Tạo CreateAuctionView.fxml
```

## 23. Commit đầu tiên đề xuất

```bash
git checkout -b dev
git checkout -b feature/project-skeleton

git add .
git commit -m "chore: initialize multi-module maven structure"
```

Commit tiếp theo:

```bash
git add .
git commit -m "docs: define socket json protocol"
```

Commit tiếp theo:

```bash
git add .
git commit -m "docs: add database schema and architecture overview"
```

## 24. Ghi chú cuối

Project này cần ưu tiên đúng thứ tự:

```text
1. Protocol JSON + Maven structure
2. Backend auction core + concurrency
3. SQLite DAO + scheduler
4. JavaFX screens với mock data
5. Socket integration thật
6. Realtime update
7. E2E testing nhiều client
8. Polish UI + optional features
```

Không làm UI quá đẹp trước khi socket/protocol/backend core ổn. Mục tiêu quan trọng nhất là demo được luồng:

```text
Register/Login -> Seller tạo auction -> Bidder xem danh sách -> Bidder đặt giá -> Client khác nhận realtime update -> Auction tự đóng -> Hiển thị winner
```
