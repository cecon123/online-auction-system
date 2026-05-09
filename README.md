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
| Backend 1 / Lead | **Huy** | Kiến trúc tổng thể, Security, Concurrency, Reviewer chính, Auth & Admin Logic |
| Backend 2 | **Mạnh** | DAO/Repository, SQLite, Unit Test Backend, CI/CD, SQL Optimization |
| Frontend 1 | **Linh** | Login/Register, Dashboard, Auction List, Filter/Search, My Bids Logic |
| Frontend 2 | **Hải Anh** | Auction Detail UI, Live Bidding, Realtime Chart, Notification System, Admin UI |

## 3. Mục tiêu chức năng

### 3.1 Chức năng bắt buộc (Core)
- [x] Đăng ký / đăng nhập tài khoản.
- [x] Role người dùng: `BIDDER`, `SELLER`, `ADMIN`.
- [x] Quản lý sản phẩm & đấu giá (Seller): Thêm/Sửa/Xóa.
- [x] Đấu giá thời gian thực: Đặt giá, kiểm tra bid hợp lệ, cập nhật highest bidder.
- [x] Concurrency: Xử lý nhiều bidder cùng lúc bằng `ReentrantLock`.
- [x] SQLite Persistence: Lưu trữ dữ liệu an toàn.

### 3.2 Chức năng đang hoàn thiện (In Progress)
- [x] Filter & Search: Lọc theo danh mục và trạng thái tại Auction List.
- [x] My Bids: Xem lại các phiên đấu giá đã tham gia.
- [ ] Notification System: Thông báo Toast khi bị vượt giá hoặc đấu giá kết thúc.
- [ ] Admin Panel: Quản lý người dùng và phiên đấu giá.

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

## 5. Cấu trúc Maven multi-module

```text
online-auction-system/
├── common/ (DTO, Enum, Model dùng chung)
├── server/ (Socket Server, Business Logic, SQLite DAO)
└── client/ (JavaFX UI, Socket Client)
```

## 6. Module dependency rule

```text
common  <- không phụ thuộc module nào
server  -> phụ thuộc common
client  -> phụ thuộc common
```

## 7. JSON protocol
Chi tiết tại `docs/protocol.md`. Tất cả message đều là một dòng JSON duy nhất kết thúc bằng ký tự xuống dòng.

## 8. Database SQLite
File schema: `server/src/main/resources/db/schema.sql`.
Sử dụng WAL mode để tăng hiệu năng concurrency.

## 9. Auction state machine
`OPEN -> RUNNING -> FINISHED -> PAID/CANCELED`

## 10. Logic đặt giá placeBid()
- Sử dụng `ReentrantLock` theo `auctionId`.
- Kiểm tra số dư, thời gian, giá hiện tại trong cùng một transaction/lock block.

## 11. Design Patterns
- **Singleton**: Database, SessionManager.
- **Factory Method**: ItemFactory.
- **Observer**: BroadcastService (Realtime updates).

## 12. UI / JavaFX design
- Sử dụng FXML cho layout và CSS cho styling.
- Giao diện hiện đại theo phong cách Material Design.

## 13. Lộ trình theo tuần (Roadmap)
- **W14 (Hiện tại):** Tích hợp và hoàn thiện (Integration & Polishing).
- **W15:** Final Testing & Demo.

## 14. Git workflow
- Branch: `feature/<name>/<task>`.
- Pull Request review bởi Huy (Lead).
- Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`.

---

## 15. Task Board (Sprint Week 14: Integration & Polishing)

Các thành viên sử dụng **Gemini CLI** hãy copy prompt tương ứng bên dưới. **Yêu cầu BẮT BUỘC: Tuân thủ nghiêm ngặt mọi quy tắc trong @GEMINI.md.**

#### 🔴 ƯU TIÊN 1: Hoàn thiện Logic Nghiệp vụ & Realtime
- **Huy (Lead):**
    - [x] Task: `GET_MY_BIDS` API & Admin Logic (`GET_USERS`, `LOCK_USER`).
- **Hải Anh:**
    - [x] Task: Auction Detail Data Binding & `NotificationManager` (Toast system).
    - [ ] **Gemini Prompt:** `Tôi là Hải Anh. Hãy thực hiện Task: 1. Code 'AuctionDetailController.java' để map 'AuctionDetailDto' vào FXML. 2. Xây dựng 'NotificationManager.java' hiển thị Toast khi nhận 'BID_UPDATE'. QUY TRÌNH: Tạo branch 'feature/haianh/detail-notif', tuân thủ @GEMINI.md, sau khi xong nhắc tôi chạy 'mvn clean install' và test thực tế. Chỉ commit/push khi hệ thống ổn định và nhắc tôi tạo PR cho Huy (Lead) review.`

#### 🟡 ƯU TIÊN 2: UI Logic & Search/Filter
- **Linh:**
    - [x] Task: Filter/Search tại `AuctionList` & `MyBidsController` implementation.
    - [x] Task: Cài đặt và sử dụng **Ikonli** để thay thế các text placeholder bằng icon.
    - [x] **Gemini Prompt:** `Tôi là Linh. Hãy thực hiện Task: 1. Code logic lọc danh sách tại 'AuctionListController.java'. 2. Triển khai 'MyBidsController.java' hiển thị lịch sử thầu. 3. Sử dụng Ikonli (FontAwesome5/MaterialDesign2) cho Sidebar/Buttons. QUY TRÌNH: Tạo branch 'feature/linh/ui-logic', tuân thủ @GEMINI.md, sau khi xong nhắc tôi chạy 'mvn clean install' và test thực tế. Chỉ commit/push khi hệ thống ổn định và nhắc tôi tạo PR cho Huy (Lead) review.`
- **Mạnh:**
    - [ ] Task: `findByBidderId` in DAO & Concurrency Stress Test.
    - [ ] **Gemini Prompt:** `Tôi là Mạnh. Hãy thực hiện Task: 1. Thêm 'findByBidderId' vào 'AuctionDao'/'SQLiteAuctionDao'. 2. Tạo test 'ConcurrentBidTest.java' (JUnit 5) giả lập 20 thread cùng bid. 3. Thêm Index tối ưu trong 'schema.sql'. QUY TRÌNH: Tạo branch 'feature/manh/stress-test-db', tuân thủ @GEMINI.md, sau khi xong nhắc tôi chạy 'mvn clean install' và 'mvn test'. Chỉ commit/push khi hệ thống ổn định và nhắc tôi tạo PR cho Huy (Lead) review.`

#### 🔵 ƯU TIÊN 3: Quản trị & Polish
- **Hải Anh:** Tích hợp LineChart dữ liệu thật.
- **Mạnh:** Tối ưu SQLite Index cho bảng `bids`.

---

## 16. Setup môi trường
- Java 21, Maven 3.9+, SQLite.
- Server: `mvn -pl server exec:java`
- Client: `mvn -pl client javafx:run`

## 17. GitHub Actions
Tự động chạy `mvn test` trên mỗi PR vào `dev` và `main`.

## 18. Test plan
- Unit tests cho Service và DAO.
- E2E Testing với nhiều client cùng lúc.

## 19. Definition of Done
- Build success, Pass Tests, Lead Approved, Merged to `dev`.

## 20. Checklist chấm điểm
- OOP, Design Patterns, Concurrency, Realtime, Client-Server, MVC.

## 21. Rủi ro và cách tránh
- SQLite Busy: Sử dụng WAL mode và Lock nghiệp vụ.
- JavaFX Threading: Luôn dùng `Platform.runLater()`.

## 22. Việc cần làm ngay (Action Items)
- Huy: Khai báo MessageType mới cho My Bids và Admin.
- Linh: Cập nhật UI Auction List để nhận sự kiện filter.

## 23. Ghi chú cuối
Tập trung vào luồng: **Register/Login -> Browse -> Bid -> Realtime Update -> Close.**

## 24. Tài liệu tham khảo
- `docs/protocol.md`
- `docs/class-diagram.md`
