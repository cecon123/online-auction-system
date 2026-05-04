# 🏛️ Online Auction System

> **Bài tập lớn – UET.CS2043 Lập trình nâng cao**  
> Học kỳ II, 2025–2026 | FIT-DSE, Vietnam National University  
> Repository: [cecon123/online-auction-system](https://github.com/cecon123/online-auction-system)

---

## 📋 Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Thành viên & Phân công](#2-thành-viên--phân-công)
3. [Công nghệ sử dụng](#3-công-nghệ-sử-dụng)
4. [Kiến trúc hệ thống](#4-kiến-trúc-hệ-thống)
5. [Cấu trúc dự án](#5-cấu-trúc-dự-án)
6. [Design Patterns](#6-design-patterns)
7. [Hướng dẫn cài đặt & chạy](#7-hướng-dẫn-cài-đặt--chạy)
8. [Git Workflow](#8-git-workflow)
9. [Lộ trình thực hiện](#9-lộ-trình-thực-hiện)
10. [JSON Protocol](#10-json-protocol)
11. [Database Schema](#11-database-schema)
12. [Thang điểm](#12-thang-điểm)

---

## 1. Giới thiệu

Hệ thống đấu giá trực tuyến (Online Auction System) theo mô hình eBay, cho phép nhiều người dùng cùng tham gia cạnh tranh giá để mua sản phẩm trong một khoảng thời gian xác định.

**Chức năng bắt buộc:**
- Quản lý người dùng (Bidder / Seller / Admin)
- Quản lý sản phẩm đấu giá (CRUD)
- Tham gia đấu giá theo thời gian thực (realtime)
- Tự động kết thúc phiên & xác định người thắng
- Xử lý lỗi & ngoại lệ toàn diện
- Giao diện GUI (JavaFX + FXML)
- Xử lý đấu giá đồng thời an toàn (concurrency)
- CI/CD tự động (GitHub Actions)

**Chức năng nâng cao (tuỳ chọn):**
- Auto-Bidding (đấu giá tự động với `maxBid`, `increment`, `PriorityQueue`)
- Anti-sniping (gia hạn phiên khi có bid trong X giây cuối)
- Bid History Visualization (LineChart giá realtime)

---

## 2. Thành viên & Phân công

| Thành viên | Vai trò | Phụ trách |
|---|---|---|
| **Huy** | Backend 1 (Lead) | Kiến trúc tổng thể, JSON protocol, SocketServer, AuthService, AuctionService (concurrency), GitHub Actions, PR review |
| **Mạnh** | Backend 2 | DAO/Repository layer, SQLite schema, AuctionScheduler, custom exceptions, unit tests (JUnit 5, JaCoCo) |
| **Linh** | Frontend 1 | Login/Register, Dashboard, AuctionList, shared layout & CSS, scene routing |
| **Hải Anh** | Frontend 2 | AuctionDetail, LiveBidding (realtime), Seller screens, LineChart realtime |

> ⚠️ **Quy tắc quan trọng:** Mỗi thành viên phải hiểu **toàn bộ codebase**. Nếu bất kỳ thành viên nào không giải thích được bất kỳ phần code nào → **toàn nhóm bị 0 điểm**. Thực hiện code review chéo qua Pull Request.

---

## 3. Công nghệ sử dụng

| Layer | Công nghệ |
|---|---|
| Language | Java 21 (LTS) |
| Build tool | Maven 3.9+ (multimodule: `common`, `server`, `client`) |
| GUI | JavaFX 21 + SceneBuilder + FXML |
| Network | Java Socket TCP (port 8080) + Gson (JSON serialization) |
| Database | SQLite 3 (WAL mode) + JDBC |
| Security | jBCrypt (password hashing) |
| Testing | JUnit 5 + JaCoCo (coverage ≥ 65%) |
| CI/CD | GitHub Actions |
| Code style | Google Java Style Guide + Checkstyle Maven plugin |
| Version control | Git + GitHub (Conventional Commits) |

---

## 4. Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│              CLIENT (JavaFX MVC)                     │
│  JavaFX Views (.fxml) ── Controllers ── SocketClient│
│  Platform.runLater()   MessageListener (Observer)    │
└──────────────────────┬──────────────────────────────┘
                       │  JSON over TCP Socket (port 8080)
┌──────────────────────▼──────────────────────────────┐
│              SERVER                                   │
│  SocketServer ── ClientHandler ×N ── RequestRouter   │
│  AuctionService (ReentrantLock)   BroadcastService   │
│  AuthService (BCrypt)             AuctionScheduler   │
└──────────────────────┬──────────────────────────────┘
                       │  JDBC (WAL mode)
┌──────────────────────▼──────────────────────────────┐
│              DATA LAYER (SQLite)                      │
│  UserDAO  AuctionDAO  BidDAO  ItemDAO  → auction.db  │
└─────────────────────────────────────────────────────┘
```

### OOP Class Hierarchy

```
Entity (abstract)
├── User (abstract)
│   ├── Bidder         – tham gia đấu giá, có balance
│   ├── Seller         – đăng sản phẩm, có shopName
│   └── Admin          – quản trị hệ thống
├── Item (abstract)
│   ├── Electronics    – thêm brand, warrantyMonths
│   ├── Art            – thêm artist, yearCreated
│   └── Vehicle        – thêm make, model, year
├── Auction            – quản lý phiên, có ReentrantLock
└── Bid                – giao dịch đặt giá
```

---

## 5. Cấu trúc dự án

```
online-auction-system/
├── pom.xml                               ← Parent POM (3 modules)
├── .github/
│   └── workflows/
│       └── ci.yml                        ← GitHub Actions CI/CD
│
├── common/                               ← Shared giữa server & client
│   └── src/main/java/com/auction/common/
│       ├── model/
│       │   ├── Entity.java               ← Abstract base class
│       │   ├── User.java                 ← Abstract
│       │   ├── Bidder.java
│       │   ├── Seller.java
│       │   ├── Admin.java
│       │   ├── Item.java                 ← Abstract
│       │   ├── Electronics.java
│       │   ├── Art.java
│       │   ├── Vehicle.java
│       │   ├── Auction.java
│       │   ├── Bid.java
│       │   ├── UserRole.java             ← Enum
│       │   ├── AuctionStatus.java        ← Enum: OPEN→RUNNING→FINISHED→PAID/CANCELED
│       │   └── ItemCategory.java         ← Enum
│       ├── protocol/
│       │   ├── MessageType.java          ← Enum tất cả message types
│       │   ├── Request.java              ← Client → Server
│       │   └── Response.java             ← Server → Client
│       └── factory/
│           └── ItemFactory.java          ← Factory Method Pattern
│
├── server/                               ← Backend
│   └── src/main/java/com/auction/server/
│       ├── ServerMain.java
│       ├── socket/
│       │   ├── SocketServer.java         ← ServerSocket + ExecutorService
│       │   └── ClientHandler.java        ← Thread per client, Runnable
│       ├── service/
│       │   ├── AuctionService.java       ← placeBid() với ReentrantLock
│       │   ├── AuthService.java          ← BCrypt login/register
│       │   └── UserService.java
│       ├── dao/
│       │   ├── UserDAO.java              ← Interface
│       │   ├── AuctionDAO.java           ← Interface
│       │   ├── BidDAO.java               ← Interface
│       │   ├── ItemDAO.java              ← Interface
│       │   └── impl/
│       │       ├── DatabaseConnection.java  ← Singleton + WAL
│       │       ├── SQLiteUserDAO.java
│       │       ├── SQLiteAuctionDAO.java
│       │       ├── SQLiteBidDAO.java
│       │       └── SQLiteItemDAO.java
│       ├── observer/
│       │   ├── AuctionObserver.java      ← Interface
│       │   ├── AuctionSubject.java       ← Interface
│       │   └── BroadcastService.java     ← notify all watchers
│       ├── scheduler/
│       │   └── AuctionScheduler.java     ← ScheduledExecutorService
│       └── exception/
│           ├── InvalidBidException.java
│           ├── AuctionClosedException.java
│           ├── AuthenticationException.java
│           └── ItemNotFoundException.java
│
└── client/                               ← JavaFX Frontend
    ├── src/main/java/com/auction/client/
    │   ├── ClientApp.java                ← Application entry point
    │   ├── network/
    │   │   ├── SocketClient.java         ← Singleton TCP client
    │   │   └── MessageListener.java      ← Observer, nhận broadcast
    │   └── controller/
    │       ├── LoginController.java
    │       ├── RegisterController.java
    │       ├── DashboardController.java
    │       ├── AuctionListController.java
    │       ├── AuctionDetailController.java
    │       ├── LiveBiddingController.java ← realtime, countdown, LineChart
    │       ├── SellerDashboardController.java
    │       ├── AddItemController.java
    │       └── AdminController.java
    └── src/main/resources/
        ├── fxml/
        │   ├── Login.fxml
        │   ├── Register.fxml
        │   ├── Dashboard.fxml
        │   ├── AuctionList.fxml
        │   ├── AuctionDetail.fxml
        │   ├── LiveBidding.fxml
        │   ├── SellerDashboard.fxml
        │   ├── AddItem.fxml
        │   └── Admin.fxml
        └── css/
            └── styles.css
```

---

## 6. Design Patterns

| Pattern | Vị trí | Mục đích |
|---|---|---|
| **Singleton** | `DatabaseConnection`, `AuctionManager`, `SocketClient` | 1 instance duy nhất, thread-safe double-checked locking |
| **Factory Method** | `ItemFactory.create(category, ...)` | Tạo `Electronics`/`Art`/`Vehicle` theo loại, dễ mở rộng |
| **Observer** | `AuctionObserver` ← `BroadcastService` ← `ClientHandler` | Notify realtime bid mới tới tất cả client đang xem phiên |
| **Strategy** *(optional)* | `ManualBidStrategy`, `AutoBidStrategy` | Xử lý logic đặt giá khác nhau |
| **Command** *(optional)* | `BidCommand` | Encapsulate bid action, hỗ trợ undo |

### Concurrency – `AuctionService.placeBid()`

```java
// Dùng ReentrantLock (KHÔNG dùng synchronized đơn giản)
// Lý do: ReentrantLock hỗ trợ tryLock() timeout, tránh deadlock với Scheduler
private final Map<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

public synchronized Bid placeBid(int auctionId, int bidderId, double amount) {
    ReentrantLock lock = auctionLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());
    try {
        if (!lock.tryLock(3, TimeUnit.SECONDS)) throw new TimeoutException("Server busy");
        Auction auction = auctionDAO.findById(auctionId).orElseThrow(...);
        if (!auction.isActive()) throw new AuctionClosedException(auctionId, auction.getStatus());
        if (amount <= auction.getCurrentPrice()) throw new InvalidBidException(auction.getCurrentPrice(), amount);
        // ... update + save + broadcast
    } finally {
        lock.unlock();
    }
}
```

---

## 7. Hướng dẫn cài đặt & chạy

### Yêu cầu

- **JDK 21** (temurin/OpenJDK): [https://adoptium.net](https://adoptium.net)
- **Maven 3.9+**: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- **JavaFX 21 SDK** (nếu không dùng Maven plugin): [https://openjfx.io](https://openjfx.io)
- **Git**: [https://git-scm.com](https://git-scm.com)
- **SceneBuilder** (để edit FXML): [https://gluonhq.com/products/scene-builder](https://gluonhq.com/products/scene-builder)

### Bước 1 – Clone repo

```bash
git clone https://github.com/cecon123/online-auction-system.git
cd online-auction-system
```

### Bước 2 – Build toàn bộ project

```bash
# Build tất cả 3 modules (common → server → client)
mvn clean install -DskipTests

# Kiểm tra build thành công
mvn validate
```

### Bước 3 – Chạy Server

```bash
# Option A: Maven (development)
mvn -pl server exec:java -Dexec.mainClass="com.auction.server.ServerMain"

# Option B: Fat JAR (sau khi build)
java -jar server/target/server-1.0.0-SNAPSHOT.jar
```

> Server khởi động tại **port 8080**. File `auction.db` được tạo tự động tại thư mục chạy lệnh.

### Bước 4 – Chạy Client (JavaFX)

```bash
# Mở terminal mới (server vẫn chạy)
mvn -pl client javafx:run
```

> Mở nhiều terminal để chạy nhiều client cùng lúc → test concurrent bidding.

### Bước 5 – Chạy Tests

```bash
# Toàn bộ test
mvn test

# Chỉ module server (nơi có logic chính)
mvn test -pl server

# Xem coverage report (sau khi test xong)
open server/target/site/jacoco/index.html    # macOS
start server/target/site/jacoco/index.html   # Windows
```

### Tài khoản test mặc định (seed data)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `seller1` | `seller123` | Seller |
| `bidder1` | `bidder123` | Bidder |
| `bidder2` | `bidder123` | Bidder |

---

## 8. Git Workflow

### Branch Strategy

```
main          ← Production, LUÔN chạy được, chỉ merge từ dev khi release
dev           ← Tích hợp, merge tất cả features vào đây
│
├─ feature/huy/project-setup
├─ feature/huy/auction-service
├─ feature/manh/dao-setup
├─ feature/manh/scheduler
├─ feature/linh/login-ui
├─ feature/linh/dashboard
├─ feature/haianh/auction-screens
└─ feature/haianh/live-bidding
```

### Quy tắc bắt buộc

1. **KHÔNG push thẳng lên `main` hoặc `dev`** → luôn dùng branch + PR
2. **Commit thường xuyên** → sau mỗi tính năng nhỏ, không để cả ngày rồi commit 1 lần
3. **PR phải được Huy review và approve** trước khi merge vào `dev`
4. **Mọi PR phải pass CI** (GitHub Actions build + test xanh) mới được merge

### Conventional Commits

```bash
# Cú pháp:  type(scope): mô tả ngắn, chữ thường
# Các type:  feat | fix | docs | refactor | test | chore | style | ci

feat(auction): add placeBid() with ReentrantLock thread-safety
feat(ui): add LiveBidding screen with realtime countdown timer
fix(dao): fix SQLite connection leak in SQLiteUserDAO
fix(concurrent): resolve race condition in AuctionService
docs(readme): add project setup and run instructions
refactor(auth): extract password hashing to AuthService class
test(auction): add JUnit5 tests for bid validation edge cases
chore(ci): setup GitHub Actions Maven build workflow
style: fix Google Java Style checkstyle violations
ci(actions): add JaCoCo coverage upload step
```

### Quy trình làm việc hàng ngày

```bash
# 1. Cập nhật dev trước khi bắt đầu làm
git checkout dev
git pull origin dev

# 2. Tạo branch mới từ dev
git checkout -b feature/[ten]/[tinh-nang]

# 3. Code, commit thường xuyên
git add .
git commit -m "feat(scope): mô tả"

# 4. Push lên remote
git push origin feature/[ten]/[tinh-nang]

# 5. Tạo Pull Request trên GitHub
#    Base: dev  ←  Compare: feature/[ten]/[tinh-nang]
#    Assign reviewer: Huy

# 6. Sau khi PR merge → dọn dẹp branch cũ
git checkout dev
git pull origin dev
git branch -d feature/[ten]/[tinh-nang]
```

### Giải quyết Merge Conflict

```bash
# Khi dev đã có thay đổi mới trong khi bạn đang làm branch
git checkout dev && git pull origin dev
git checkout feature/[ten]/[tinh-nang]
git merge dev                    # merge dev mới nhất vào branch của mình
# Giải quyết conflict trong IDE → git add . → git commit
git push origin feature/[ten]/[tinh-nang]
```

---

## 9. Lộ trình thực hiện

### W6 – Khởi động & Thiết kế OOP ← **Hiện tại**

| Thành viên | Việc cần làm |
|---|---|
| **Huy** | Setup Maven multimodule, .gitignore, CI workflow, class hierarchy (Entity→User→Bidder/Seller/Admin, Item→Electronics/Art/Vehicle), Singleton AuctionManager, Factory ItemFactory, protect branches |
| **Mạnh** | DAO interfaces (UserDAO, AuctionDAO, BidDAO, ItemDAO), DatabaseConnection Singleton + SQLite schema + index, custom exceptions |
| **Linh** | Cài JavaFX + SceneBuilder, ClientApp.java entry point, Login.fxml + LoginController, Register.fxml, global styles.css |
| **Hải Anh** | Nghiên cứu JavaFX FXML binding, prototype AuctionList.fxml, AuctionDetail.fxml, LiveBidding.fxml skeleton, wireframe toàn bộ màn hình |

### W7 – Concurrency & Observer Pattern

| Thành viên | Việc cần làm |
|---|---|
| **Huy** | `AuctionService.placeBid()` với `ReentrantLock`, Observer pattern (interface + BroadcastService), JSON protocol document, SocketServer skeleton |
| **Mạnh** | SQLiteUserDAO/AuctionDAO/BidDAO/ItemDAO hoàn chỉnh, AuctionScheduler, state machine OPEN→RUNNING→FINISHED→PAID/CANCELED |
| **Linh** | LoginController kết nối mock service, DashboardView, AuctionListController với ObservableList mock |
| **Hải Anh** | AuctionDetailController với mock data, LiveBidding binding, countdown timer (JavaFX Timeline) |

### W8 – Exception Handling & Unit Testing

| Thành viên | Việc cần làm |
|---|---|
| **Huy** | AuthService (BCrypt), SocketServer hoàn chỉnh (JSON parse → route), Gson serialization, SOLID refactoring |
| **Mạnh** | JUnit 5 tests (AuctionServiceTest, StateTransitionTest), JaCoCo setup, test với in-memory SQLite (`:memory:`) |
| **Linh** | RegisterController validation, shared layout component, scene switching |
| **Hải Anh** | SellerDashboard, AddItemView, EditItemView |

### W9 – CI/CD & Socket Integration

| Thành viên | Việc cần làm |
|---|---|
| **Huy** | Tích hợp SocketServer + real services, SocketClient module client, GitHub Actions, Maven Shade Plugin (fat JAR) |
| **Mạnh** | Checkstyle Maven plugin, Serialization backup, unit tests mở rộng |
| **Linh** | Login/Register kết nối SocketClient thật, xử lý lỗi kết nối (Alert dialog) |
| **Hải Anh** | AuctionDetail/LiveBidding kết nối server thật, countdown timer đổi màu < 10s |

### W10 – Realtime Full & GUI Hoàn thiện

| Thành viên | Việc cần làm |
|---|---|
| **Huy** | BroadcastService thread-safe (`CopyOnWriteArrayList`), review tất cả PR |
| **Mạnh** | SQLite WAL mode (`PRAGMA journal_mode=WAL`), coverage ≥ 65% |
| **Linh** | Client nhận `BID_UPDATE` → `Platform.runLater()`, notification toast |
| **Hải Anh** | LiveBidding nhận broadcast realtime, LineChart `<String,Number>` cập nhật mỗi bid |

### W11–12 – Tích hợp toàn bộ & E2E Testing

- Merge tất cả features, test Server + 3-4 Client đồng thời
- Fix bugs, edge cases, CI/CD xanh hoàn toàn

### W13–14 – Polish & Chức năng nâng cao

- Auto-Bidding: `PriorityQueue<AutoBid>`, tự tăng giá, không vượt `maxBid` (+0.5đ)
- Anti-sniping: bid trong 60s cuối → gia hạn thêm 60s (+0.5đ)
- LineChart realtime hoàn chỉnh với tooltip (+0.5đ)
- Viết README đầy đủ, video demo backup

### W15 – Trình bày & Chấm điểm

- Demo trực tiếp: Server + 3+ Client đồng thời
- Mỗi thành viên giải thích phần code của mình và cross-module
- Phân chia điểm theo đóng góp thực tế

---

## 10. JSON Protocol

Tất cả giao tiếp Client ↔ Server qua JSON string. Định nghĩa từ W6 để frontend có thể mock song song.

### Client → Server (Request)

```json
// Đăng nhập
{ "type": "LOGIN", "payload": { "username": "alice", "password": "hashed" } }

// Đăng ký
{ "type": "REGISTER", "payload": { "username": "bob", "email": "bob@x.com", "password": "hashed", "role": "BIDDER" } }

// Đặt giá
{ "type": "PLACE_BID", "token": "session-token-abc", "payload": { "auctionId": 1, "amount": 1500.0 } }

// Lấy danh sách phiên
{ "type": "GET_AUCTIONS", "token": "...", "payload": { "status": "RUNNING" } }

// Xem realtime phiên
{ "type": "WATCH_AUCTION",   "token": "...", "payload": { "auctionId": 1 } }
{ "type": "UNWATCH_AUCTION", "token": "...", "payload": { "auctionId": 1 } }

// Tạo item mới (Seller)
{ "type": "CREATE_ITEM", "token": "...", "payload": { "name": "iPhone 16", "description": "...", "startPrice": 500.0, "category": "ELECTRONICS" } }
```

### Server → Client (Response direct)

```json
// Kết quả đăng nhập
{ "type": "LOGIN_RESULT",  "success": true,  "payload": { "token": "session-abc", "role": "BIDDER", "userId": 3 } }
{ "type": "LOGIN_RESULT",  "success": false, "error": "Invalid username or password" }

// Kết quả đặt giá
{ "type": "BID_RESULT",    "success": true,  "payload": { "newAmount": 1500.0, "auctionId": 1 } }
{ "type": "BID_RESULT",    "success": false, "error": "Bid must be higher than current price $1200.00" }
```

### Server → ALL watching clients (Broadcast)

```json
// Có bid mới – broadcast tới tất cả đang WATCH_AUCTION auctionId=1
{ "type": "BID_UPDATE",     "payload": { "auctionId": 1, "amount": 1500.0, "bidder": "alice", "timestamp": "2026-04-15T20:01:23" } }

// Phiên kết thúc
{ "type": "AUCTION_CLOSED", "payload": { "auctionId": 1, "winner": "alice", "finalAmount": 1500.0 } }

// Anti-sniping: gia hạn thêm
{ "type": "TIME_EXTENDED",  "payload": { "auctionId": 1, "newEndTime": "2026-04-15T20:02:00" } }
```

---

## 11. Database Schema

```sql
-- Bật WAL mode khi khởi động (trong DatabaseConnection.java)
PRAGMA journal_mode=WAL;
PRAGMA foreign_keys=ON;
PRAGMA busy_timeout=3000;

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT UNIQUE NOT NULL,
    email         TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,        -- BCrypt hash, KHÔNG lưu password rõ
    role          TEXT NOT NULL,        -- BIDDER | SELLER | ADMIN
    shop_name     TEXT,                 -- chỉ dùng cho Seller
    balance       REAL DEFAULT 0.0,    -- chỉ dùng cho Bidder
    created_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT NOT NULL,
    description  TEXT,
    start_price  REAL NOT NULL,
    category     TEXT NOT NULL,         -- ELECTRONICS | ART | VEHICLE | OTHER
    seller_id    INTEGER NOT NULL REFERENCES users(id),
    -- Electronics
    brand        TEXT,
    -- Art
    artist       TEXT,
    year_created INTEGER,
    -- Vehicle
    make         TEXT,
    model        TEXT,
    year         INTEGER,
    created_at   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS auctions (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id           INTEGER NOT NULL REFERENCES items(id),
    seller_id         INTEGER NOT NULL REFERENCES users(id),
    current_price     REAL NOT NULL,
    current_leader_id INTEGER REFERENCES users(id),   -- NULL = chưa có bid
    status            TEXT NOT NULL DEFAULT 'OPEN',   -- OPEN|RUNNING|FINISHED|PAID|CANCELED
    start_time        TEXT NOT NULL,
    end_time          TEXT NOT NULL,
    created_at        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS bids (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL REFERENCES auctions(id),
    bidder_id  INTEGER NOT NULL REFERENCES users(id),
    amount     REAL NOT NULL,
    timestamp  TEXT NOT NULL
);

-- Indexes để tăng tốc query
CREATE INDEX IF NOT EXISTS idx_bids_auction    ON bids(auction_id);
CREATE INDEX IF NOT EXISTS idx_bids_bidder     ON bids(bidder_id);
CREATE INDEX IF NOT EXISTS idx_auctions_status ON auctions(status);
CREATE INDEX IF NOT EXISTS idx_auctions_seller ON auctions(seller_id);
CREATE INDEX IF NOT EXISTS idx_items_seller    ON items(seller_id);
```

---

## 12. Thang điểm

| Nội dung đánh giá | Điểm | Mức | Phụ trách |
|---|---|---|---|
| Thiết kế lớp & cây kế thừa (User/Item hierarchy) | 0.5 | Bắt buộc | Huy + Mạnh |
| Áp dụng OOP (Encapsulation, Inheritance, Polymorphism, Abstraction) | 1.0 | Bắt buộc | Huy + Mạnh |
| Design Patterns (Singleton, Factory Method, Observer) | 1.0 | Bắt buộc | Huy |
| Quản lý người dùng & sản phẩm (CRUD) | 1.0 | Bắt buộc | Huy + Mạnh + Linh |
| Chức năng đấu giá (placeBid, kết thúc, xác định winner) | 1.0 | Bắt buộc | Huy + Mạnh + Hải Anh |
| Xử lý lỗi & ngoại lệ (custom exceptions) | 1.0 | Bắt buộc | Mạnh |
| Concurrent bidding an toàn (ReentrantLock, tránh lost update) | 1.0 | Bắt buộc | Huy |
| Realtime update – Observer/Socket broadcast | 0.5 | Bắt buộc | Huy + Hải Anh |
| Kiến trúc Client–Server rõ ràng | 0.5 | Bắt buộc | Huy |
| MVC: JavaFX + FXML (client), Controller–Service–DAO (server) | 0.5 | Bắt buộc | Linh + Hải Anh |
| Maven, Google Java Style Guide, mã nguồn sạch | 0.5 | Bắt buộc | Huy + Mạnh |
| Unit Test JUnit 5 (≥ 65% line coverage) | 0.5 | Bắt buộc | Mạnh |
| CI/CD GitHub Actions (tự động build + test khi push) | 0.5 | Bắt buộc | Huy + Mạnh |
| **Tổng bắt buộc** | **8.5** | | |
| Auto-Bidding (maxBid, increment, PriorityQueue) | +0.5 | Tuỳ chọn | Huy |
| Anti-sniping (gia hạn phiên khi bid trong X giây cuối) | +0.5 | Tuỳ chọn | Huy + Mạnh |
| Bid History Visualization (LineChart realtime) | +0.5 | Tuỳ chọn | Hải Anh |
| **Tổng tối đa** | **10.0** | | |

---

## Tài liệu tham khảo

### JavaFX
- Cài đặt: https://openjfx.io/openjfx-docs/
- Playlist cơ bản: https://www.youtube.com/watch?v=_7OM-cMYWbQ&list=PLZPZq0r_RZOM-8vJA3NQFZB7JroDcMwev
- Setup SceneBuilder + IntelliJ: https://www.youtube.com/watch?v=IZCwawKILsk
- MVC trong JavaFX: https://www.pragmaticcoding.ca/javafx/MVC_In_JavaFX
- Realtime LineChart: https://www.youtube.com/watch?v=HWfZPiPu1sI

### Lập trình mạng (Socket)
- Cơ bản Socket Java: https://www.youtube.com/watch?v=plh_cIEQ1Jo
- Baeldung guide: https://www.baeldung.com/a-guide-to-java-sockets
- Chat app JavaFX + Socket: https://www.youtube.com/watch?v=_1nqY-DKP9A

### Design Patterns
- Refactoring Guru: https://refactoring.guru/design-patterns
- Video playlist: https://www.youtube.com/watch?v=mE3qTp1TEbg&list=PLlsmxlJgn1HJpa28yHzkBmUY-Ty71ZUGc

### Testing & CI
- JUnit 5 User Guide: https://docs.junit.org/5.5.0/user-guide/
- GitHub Actions + Maven: https://www.youtube.com/watch?v=UTb3nNbH7M4
- Checkstyle + Maven: https://medium.com/@sruthiganesh/integrating-checkstyle-in-java-projects-with-maven-b1ac2cafd016

### Dự án tham khảo trên GitHub
- https://github.com/nlintas/Auction-System-in-Java
- https://github.com/gangulwar/socket-programming-auction-system
- https://github.com/Prasanna-icefire/AuctionSystem

---

*Last updated: W6 – May 2026 | Nhóm dự án UET.CS2043*
