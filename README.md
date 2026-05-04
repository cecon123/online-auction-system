# Online Auction System - AuctionPro

> Bài tập lớn Lập trình nâng cao 2026: xây dựng hệ thống đấu giá trực tuyến theo kiến trúc **Client-Server**, sử dụng **Java 21**, **JavaFX**, **Socket JSON**, **SQLite**, **Maven multi-module**, **JUnit 5** và **Git/GitHub**.

## 1. Thông tin dự án

- **Tên dự án:** Online Auction System / AuctionPro
- **Repository:** <https://github.com/cecon123/online-auction-system>
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

## 2. Thành viên và phân công chính

| Vai trò | Thành viên | Phụ trách chính |
|---|---|---|
| Backend 1 / Lead | Huy | Kiến trúc tổng thể, Maven multi-module, JSON protocol, socket server, auth, auction core, concurrency, review tích hợp |
| Backend 2 | Mạnh | DAO/Repository, SQLite, scheduler, realtime backend, custom exceptions, unit test backend, CI/CD |
| Frontend 1 | Linh | Login/Register, dashboard, auction list, AppShell, layout chung, CSS JavaFX |
| Frontend 2 | Hải Anh | Auction detail, live bidding screen, seller screens, realtime chart, admin UI tối giản |

Lead không có nghĩa là code hết. Lead chịu trách nhiệm khóa kiến trúc, protocol, tiêu chuẩn merge và đảm bảo mọi module tích hợp được.

---

## 3. Current Progress

### 3.1 Đã hoàn thành

- [x] Maven multi-module: `common`, `server`, `client`
- [x] Java 21 build với Maven
- [x] Server chạy được bằng:

```bash
mvn -pl server exec:java
```

- [x] JavaFX client chạy được bằng:

```bash
mvn -pl client javafx:run
```

- [x] JSON protocol wrapper:
  - `MessageType`
  - `Request<T>`
  - `Response<T>`
- [x] Socket server skeleton
- [x] `ClientHandler` đọc JSON request qua socket
- [x] `RequestRouter` mock
- [x] Test socket bằng `ncat localhost 8080` thành công với `LOGIN`
- [x] SQLite schema nền:
  - `users`
  - `items`
  - `auctions`
  - `bids`
  - `auto_bids`
- [x] `Database` singleton
- [x] `SchemaInitializer`
- [x] `AppProperties` đọc `application.properties`
- [x] `UserDao`
- [x] `SQLiteUserDao`
- [x] Unit test cho `SQLiteUserDao`

### 3.2 Đang làm

- [ ] Common domain model:
  - `Entity`
  - `User`, `Bidder`, `Seller`, `Admin`
  - `Item`, `Electronics`, `Art`, `Vehicle`
  - `Auction`
  - `BidTransaction`
  - `AutoBidRule`
- [ ] `docs/class-diagram.md`
- [ ] Model inheritance tests

### 3.3 Làm ngay sau đó

- [ ] `ItemFactory` - Factory Method
- [ ] `docs/design-patterns.md`
- [ ] `CODEOWNERS`
- [ ] `ItemDao`, `AuctionDao`, `BidDao`
- [ ] Exception hierarchy
- [ ] `AuthService` thật
- [ ] JavaFX AppShell + Sidebar + TopBar
- [ ] Auction Detail + Live Bidding mock UI

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
  - Không có rollback giá.
  - Không có hai người cùng thắng.
- OOP:
  - Encapsulation
  - Inheritance
  - Polymorphism
  - Abstraction
- Design Patterns:
  - Singleton
  - Factory Method
  - Observer
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
- Các DTO, enum, model dùng chung đặt trong module `common`.
- UI JavaFX tách theo MVC: FXML là View, Controller xử lý UI, Service client gọi socket.
- Server tách theo Controller -> Service -> DAO.

---

## 6. Cấu trúc Maven multi-module

### 6.1 Cấu trúc hiện tại / mục tiêu gần

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
│   ├── class-diagram.md
│   ├── protocol.md
│   ├── architecture.md
│   ├── database-schema.md
│   ├── design-patterns.md
│   ├── git-workflow.md
│   ├── test-plan.md
│   ├── demo-script.md
│   └── ui-design.md
│
├── common/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/auction/common/
│       │   ├── enums/
│       │   │   ├── Role.java
│       │   │   ├── AuctionStatus.java
│       │   │   ├── ItemType.java
│       │   │   ├── ResponseStatus.java
│       │   │   └── BidType.java
│       │   │
│       │   ├── protocol/
│       │   │   ├── MessageType.java
│       │   │   ├── Request.java
│       │   │   ├── Response.java
│       │   │   └── ErrorResponse.java
│       │   │
│       │   ├── dto/
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
│       │   ├── exception/
│       │   └── util/
│       │
│       └── test/java/com/auction/common/
│
├── server/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/auction/server/
│       │   │   ├── ServerMain.java
│       │   │   ├── socket/
│       │   │   │   ├── SocketServer.java
│       │   │   │   ├── ClientHandler.java
│       │   │   │   └── RequestRouter.java
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── dao/
│       │   │   │   ├── Database.java
│       │   │   │   ├── SchemaInitializer.java
│       │   │   │   ├── UserDao.java
│       │   │   │   ├── ItemDao.java
│       │   │   │   ├── AuctionDao.java
│       │   │   │   ├── BidDao.java
│       │   │   │   └── sqlite/
│       │   │   │       ├── SQLiteUserDao.java
│       │   │   │       ├── SQLiteItemDao.java
│       │   │   │       ├── SQLiteAuctionDao.java
│       │   │   │       └── SQLiteBidDao.java
│       │   │   ├── mapper/
│       │   │   ├── factory/
│       │   │   │   └── ItemFactory.java
│       │   │   ├── observer/
│       │   │   ├── scheduler/
│       │   │   ├── security/
│       │   │   ├── concurrency/
│       │   │   ├── config/
│       │   │   │   └── AppProperties.java
│       │   │   └── util/
│       │   │       └── JsonMapper.java
│       │   │
│       │   └── resources/
│       │       ├── application.properties
│       │       └── db/
│       │           ├── schema.sql
│       │           ├── seed.sql
│       │           └── test-seed.sql
│       │
│       └── test/java/com/auction/server/
│
└── client/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/auction/client/
        │   │   ├── ClientMain.java
        │   │   ├── app/
        │   │   ├── network/
        │   │   ├── controller/
        │   │   ├── viewmodel/
        │   │   ├── service/
        │   │   ├── component/
        │   │   └── util/
        │   │
        │   └── resources/
        │       ├── fxml/
        │       ├── css/
        │       ├── images/
        │       └── fonts/
        │
        └── test/java/com/auction/client/
```

### 6.2 Module dependency rule

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

Client chỉ gọi server qua:

```java
SocketClient.send(Request<?> request);
```

---

## 7. Cách chạy dự án

### 7.1 Yêu cầu môi trường

- JDK 21
- Maven 3.9+
- Git
- JavaFX dependency được Maven kéo tự động
- SQLite JDBC dependency được Maven kéo tự động

Kiểm tra:

```bash
java -version
mvn -version
git --version
```

### 7.2 Clone project

```bash
git clone https://github.com/cecon123/online-auction-system.git
cd online-auction-system
```

### 7.3 Build toàn bộ project

```bash
mvn clean install
```

### 7.4 Chạy server

```bash
mvn -pl server exec:java
```

Server mặc định chạy ở port `8080`, cấu hình trong:

```text
server/src/main/resources/application.properties
```

```properties
server.port=8080
database.url=jdbc:sqlite:auction.db
database.enableWal=true
database.busyTimeoutMs=5000
```

### 7.5 Chạy client

Mở terminal khác:

```bash
mvn -pl client javafx:run
```

### 7.6 Test socket nhanh bằng ncat

Khi server đang chạy:

```bash
ncat localhost 8080
```

Gửi JSON:

```json
{"type":"LOGIN","requestId":"req-001","token":null,"data":{"username":"huy","password":"123456"}}
```

Response mock kỳ vọng:

```json
{
  "type": "LOGIN",
  "requestId": "req-001",
  "success": true,
  "message": "Mock login successful",
  "data": {
    "userId": 1,
    "username": "mock-user",
    "role": "BIDDER",
    "token": "mock-session-token"
  }
}
```

---

## 8. JSON protocol

### 8.1 Request format

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

### 8.2 Response format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "success": true,
  "message": "Bid accepted",
  "data": {
    "auctionId": 1,
    "currentPrice": 1500000,
    "highestBidderUsername": "huy"
  }
}
```

### 8.3 Realtime event format

```json
{
  "type": "BID_UPDATE",
  "requestId": null,
  "success": true,
  "message": "New bid received",
  "data": {
    "auctionId": 1,
    "amount": 1500000,
    "bidderUsername": "huy",
    "timestamp": "2026-05-04T20:30:00",
    "newEndTime": null
  }
}
```

### 8.4 Message types tối thiểu

```text
REGISTER
LOGIN
LOGOUT

GET_DASHBOARD

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
ADMIN_UPDATE_USER_STATUS
ADMIN_GET_AUCTIONS
```

---

## 9. Database SQLite

File chính:

```text
server/src/main/resources/db/schema.sql
server/src/main/resources/db/seed.sql
server/src/main/resources/db/test-seed.sql
```

### 9.1 `users`

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

### 9.2 `items`

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

### 9.3 `auctions`

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

### 9.4 `bids`

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

### 9.5 `auto_bids`

```sql
CREATE TABLE IF NOT EXISTS auto_bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    max_bid REAL NOT NULL,
    increment REAL NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);
```

### 9.6 SQLite PRAGMA bắt buộc

Khi mở connection, bật:

```sql
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA busy_timeout = 5000;
```

Lưu ý: WAL giúp giảm lỗi `SQLITE_BUSY`, nhưng không thay thế lock nghiệp vụ trong `BidService.placeBid()`.

---

## 10. Domain model và class diagram

Class diagram được đặt tại:

```text
docs/class-diagram.md
```

Cây kế thừa chính:

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

Relationship summary:

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

## 11. Auction state machine

```text
OPEN -> RUNNING -> FINISHED -> PAID
                    |
                    v
                 CANCELED
```

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

---

## 12. Logic đặt giá `placeBid()`

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

        BidTransaction bid = bidDao.create(auctionId, bidderId, amount);
        auctionDao.updateHighestBid(auctionId, bidderId, amount, auction.getVersion());

        broadcastService.broadcastBidUpdate(auctionId, bid);

        return new PlaceBidResponse(...);
    } finally {
        lock.unlock();
    }
}
```

Yêu cầu bắt buộc:

```text
- Dùng ReentrantLock theo auctionId.
- Không dùng synchronized toàn service vì dễ bottleneck.
- Không cho seller tự bid.
- Không cho bid khi auction chưa RUNNING hoặc đã FINISHED/CANCELED.
- Không cho bid thấp hơn hoặc bằng currentPrice.
- Sau bid hợp lệ phải lưu BidTransaction.
- Sau bid hợp lệ phải update currentPrice và highestBidderId.
- Sau bid hợp lệ phải broadcast BID_UPDATE.
```

---

## 13. Design Patterns

### 13.1 Singleton

Áp dụng cho:

```text
Database.getInstance()
AppProperties.getInstance()
```

Có thể bổ sung:

```text
AuctionLockManager.getInstance()
```

Mục đích:

- Quản lý cấu hình và tài nguyên dùng chung.
- Tránh tạo nhiều object quản lý database/lock không cần thiết.

### 13.2 Factory Method

Áp dụng cho:

```text
ItemFactory.create(...)
```

Mapping:

```text
ItemType.ELECTRONICS -> Electronics
ItemType.ART         -> Art
ItemType.VEHICLE     -> Vehicle
```

Mục đích:

- Không để service/controller tự `new Electronics`, `new Art`, `new Vehicle` rải rác.
- Dễ mở rộng thêm loại item mới.

### 13.3 Observer

Áp dụng cho realtime bidding:

```text
BidService
-> BroadcastService
-> ClientConnectionRegistry
-> ClientHandler
-> JavaFX Client
```

Khi có bid mới:

```text
PLACE_BID accepted
-> tạo BidTransaction
-> update Auction
-> broadcast BID_UPDATE tới client đang xem auction đó
```

---

## 14. Branch strategy và Git workflow

### 14.1 Branch chính

```text
main: code ổn định để nộp/demo
 dev: branch tích hợp chính của nhóm
```

Không push trực tiếp vào `main`.

### 14.2 Feature branch

Format:

```text
feature/<task-name>-<member-name>
```

Ví dụ:

```text
feature/project-skeleton-huy
feature/protocol-router-huy
feature/sqlite-userdao-manh
feature/common-model-class-diagram-huy
feature/item-factory-huy
feature/app-shell-dashboard-linh
feature/auction-detail-live-seller-ui-haianh
```

### 14.3 Quy trình làm việc

Luôn bắt đầu từ `dev` mới nhất:

```bash
git checkout dev
git pull origin dev
git checkout -b feature/task-name-yourname
```

Sau khi code:

```bash
mvn clean install
git status
git add .
git commit -m "feat: short description"
git push -u origin feature/task-name-yourname
```

Tạo Pull Request:

```text
base: dev
compare: feature/task-name-yourname
```

Sau khi review pass, merge vào `dev`.

### 14.4 Conventional Commits

Dùng format:

```text
feat: thêm chức năng mới
fix: sửa lỗi
chore: cấu hình / việc phụ trợ
refactor: refactor không đổi behavior
test: thêm/sửa test
docs: cập nhật tài liệu
style: format code
```

Ví dụ:

```bash
git commit -m "feat: add sqlite user dao"
git commit -m "fix: handle invalid login request"
git commit -m "docs: update class diagram"
```

---

## 15. Updated Project Plan

### 15.1 W6 - Khởi động, Thiết kế OOP & JavaFX skeleton

Mục tiêu khóa W6:

- [x] Tạo GitHub repository, `main`, `dev`
- [x] Maven multi-module
- [x] Server skeleton
- [x] Client JavaFX skeleton
- [x] JSON protocol mock
- [x] SQLite schema + UserDao
- [ ] Common model đầy đủ
- [ ] Class diagram đầy đủ
- [ ] Factory Method `ItemFactory`
- [ ] Document Singleton/Factory/Observer trong `docs/design-patterns.md`
- [ ] CODEOWNERS

Branch cần có:

```text
feature/project-skeleton-huy              DONE
feature/protocol-router-huy               DONE
feature/sqlite-userdao-manh               DONE
feature/common-model-class-diagram-huy    IN PROGRESS
feature/item-factory-huy                  NEXT
```

### 15.2 W7 - Concurrency & Observer Pattern

Huy:

```text
feature/auction-core-locking-huy
- LockRegistry
- AuctionLockManager
- AuctionService.placeBid() dùng ReentrantLock
- Validate seller không tự bid
- Validate amount > currentPrice
```

Mạnh:

```text
feature/sqlite-item-auction-bid-dao-manh
- SQLiteItemDao
- SQLiteAuctionDao
- SQLiteBidDao
- DAO tests
```

Linh:

```text
feature/login-dashboard-auctionlist-linh
- LoginController validate form
- RegisterController validate form
- Dashboard mock
- AuctionList mock
```

Hải Anh:

```text
feature/auction-detail-live-ui-haianh
- AuctionDetail mock
- LiveBidding mock
- Bid history table
- Countdown label
```

### 15.3 W8 - Exception Handling & Unit Testing

Huy:

```text
feature/auth-service-huy
- PasswordHasher
- SessionManager
- AuthService
- AuthController
- Router LOGIN/REGISTER thật
```

Mạnh:

```text
feature/exceptions-tests-manh
- AuctionException
- InvalidBidException
- AuctionClosedException
- AuthenticationException
- AuthorizationException
- NotFoundException
- ValidationException
- DataAccessException
- AuctionServiceTest
- BidServiceTest
```

Linh:

```text
feature/frontend-validation-linh
- Register form validation
- Login error box
- Shared CSS
- Navigation Login/Register/AppShell
```

Hải Anh:

```text
feature/seller-screens-haianh
- SellerCenterView
- CreateAuctionView
- EditAuctionView
```

### 15.4 W9 - CI/CD & Socket Integration

Huy:

```text
feature/socket-integration-huy
- RequestRouter gọi controller thật
- ClientHandler quản lý session
- ResponseFactory
- Server error handling chuẩn JSON
```

Mạnh:

```text
feature-ci-checkstyle-serialization-manh
- GitHub Actions ổn định
- Checkstyle Maven
- seed.sql
- shutdown hook save/load nếu cần
```

Linh:

```text
feature/client-socket-auth-linh
- SocketClient
- ClientAuthService
- Login/Register gọi server thật
```

Hải Anh:

```text
feature/client-socket-auction-haianh
- AuctionDetail gọi GET_AUCTION_DETAIL
- LiveBidding gửi PLACE_BID
```

### 15.5 W10 - Full Realtime & GUI hoàn thiện

Huy:

```text
feature/broadcast-service-huy
- ClientSession
- ClientConnectionRegistry
- BroadcastService
- SUBSCRIBE_AUCTION
- UNSUBSCRIBE_AUCTION
```

Mạnh:

```text
feature/scheduler-realtime-backend-manh
- AuctionScheduler thật
- OPEN -> RUNNING
- RUNNING -> FINISHED
- Broadcast AUCTION_CLOSED
```

Linh:

```text
feature/realtime-auction-list-linh
- AuctionList nhận BID_UPDATE
- Dashboard refresh từ server
```

Hải Anh:

```text
feature/realtime-live-bidding-haianh
- LiveBidding nhận BID_UPDATE
- Platform.runLater()
- LineChart thêm point realtime
```

### 15.6 W11-12 - Tích hợp toàn bộ & E2E Testing

- [ ] Server + 3-4 Client JVM đồng thời
- [ ] E2E test flow:
  - register
  - login
  - browse auctions
  - bid
  - realtime update
  - auction closed
- [ ] Fix merge conflicts
- [ ] Fix database edge cases
- [ ] Đảm bảo restart server không mất dữ liệu
- [ ] CI/CD xanh

### 15.7 W13-14 - Polish & Chức năng nâng cao

Ưu tiên nâng cao:

1. Bid History Visualization - Hải Anh
2. Anti-sniping - Huy + Mạnh
3. Auto-Bidding - Huy nếu core đã ổn

Không làm nâng cao nếu core chưa ổn.

### 15.8 W15 - Demo & Chấm điểm

- [ ] Chuẩn bị slide trình bày
- [ ] Demo Server + nhiều Client
- [ ] Mỗi thành viên giải thích được phần mình và hiểu luồng tổng thể
- [ ] Chuẩn bị demo script và backup video

---

## 16. Testing plan

### 16.1 Chạy toàn bộ test

```bash
mvn clean test
```

### 16.2 Chạy test module common

```bash
mvn -pl common test
```

### 16.3 Chạy test module server

```bash
mvn -pl server test
```

### 16.4 Target coverage

- W8: >= 60%
- W10: >= 65%
- W13-14: >= 70% nếu kịp

Test bắt buộc:

```text
- Request/Response serialization
- UserDao CRUD
- ItemFactory
- Auction state transition
- Valid bid
- Invalid bid amount
- Bid when auction closed
- Seller bids own auction
- Concurrent bidding
- Auth login/register
- Scheduler close auction
```

---

## 17. UI design direction

UI theo phong cách **AuctionPro - Premium Bidding**.

Màn hình chính:

```text
- Login
- Register
- AppShell
- Dashboard
- AuctionList
- AuctionDetail
- LiveBidding
- MyBids
- SellerCenter
- CreateAuction
- EditAuction
- AdminPanel
```

Style:

```text
- Sidebar màu indigo đậm
- TopBar trắng
- Background sáng nhẹ
- Card bo góc
- Button primary màu indigo
- Status badge rõ OPEN/RUNNING/FINISHED/CANCELED
- Currency và timer dùng font mono nếu có thể
```

JavaFX implementation:

```text
- FXML cho layout
- Controller xử lý event UI
- Client service gọi socket
- Không đưa business logic vào controller
- Dữ liệu table/list dùng ObservableList
- Realtime update UI dùng Platform.runLater()
```

---

## 18. Quy tắc code review

Mỗi Pull Request cần có:

```markdown
## Nội dung
- Mô tả thay đổi chính

## Cách test
```bash
mvn clean install
```

## Checklist
- [ ] Build pass
- [ ] Test pass
- [ ] Không import sai module
- [ ] Không commit file runtime như `.db`, `target/`
- [ ] Có giải thích ngắn trong README/docs nếu là phần kiến trúc
```

Reviewer cần kiểm tra:

```text
- Code có đúng package không
- Có phá module dependency không
- Có hard-code magic string quá nhiều không
- Có test cho logic quan trọng không
- Có xử lý exception không
- Có ảnh hưởng người khác không
```

---

## 19. File không được commit

Không commit:

```text
*.db
*.db-shm
*.db-wal
target/
.idea/
*.iml
.DS_Store
```

Nếu lỡ tạo `auction.db`, kiểm tra `.gitignore` đã ignore chưa.

---

## 20. Quick command reference

```bash
# Build all
mvn clean install

# Run server
mvn -pl server exec:java

# Run client
mvn -pl client javafx:run

# Test all
mvn clean test

# Test server only
mvn -pl server test

# Start feature branch
git checkout dev
git pull origin dev
git checkout -b feature/task-name-yourname

# Commit feature
git status
git add .
git commit -m "feat: short description"
git push -u origin feature/task-name-yourname
```

---

## 21. Notes for presentation

Mỗi thành viên cần nắm được:

```text
- Vì sao dùng Client-Server
- Vì sao client không truy cập SQLite
- Request/Response JSON hoạt động thế nào
- DAO khác Service thế nào
- OOP thể hiện ở model nào
- Singleton/Factory/Observer nằm ở đâu
- Vì sao placeBid cần ReentrantLock
- Realtime update đi từ server sang client như thế nào
- JavaFX MVC tách View/Controller/Service ra sao
```

Nếu một người không giải thích được phần code, cả nhóm có rủi ro mất điểm rất nặng. Vì vậy mọi PR cần được review chéo và mọi người phải đọc code của nhau.
