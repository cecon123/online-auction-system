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
- **W14 (Hoàn thành):** Tích hợp và hoàn thiện (Integration & Polishing).
- **W15 (Hiện tại):** Kiểm thử cuối cùng, Tối ưu UX & Sẵn sàng Demo (Final Testing & Demo Readiness).

## 14. Git workflow
- Branch: `feature/<name>/<task>`. (VD: `feature/huy/final-audit`)
- Pull Request review bởi Huy (Lead).
- Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`.

---

## 15. Task Board (Sprint Week 15: Final Polish & Demo)

Các thành viên sử dụng **Gemini CLI** hãy tuân thủ nghiêm ngặt mọi quy tắc trong @GEMINI.md.

### ⚙️ Mạnh (Backend)
- **Branch:** `feature/manh/refactor-and-audit`
- **Tasks:**
    - [ ] **Refactor `LiveBiddingController`:** Tách logic Chart, Countdown và BidHistory sang các Manager/Helper độc lập để giảm kích thước file.
    - [ ] **Database & Concurrency Audit:** Kiểm tra kích hoạt `WAL Mode` trong code, thêm index cho `auto_bids(bidder_id)` và đồng nhất logic `BigDecimal`.
    - [ ] **Global Exception Handling:** Cài đặt bộ bắt lỗi tập trung (`UncaughtExceptionHandler`) tại Client và Server để tránh crash đột ngột.
    - [ ] Hoàn thiện JavaDoc cho các Service core để hỗ trợ bảo trì lâu dài.
- **Gemini Prompt:**
  > "Gemini, hãy giúp tôi tách logic xử lý LineChart trong `LiveBiddingController` ra một class `PriceChartManager`. Sau đó, kiểm tra class `Database` xem lệnh `PRAGMA journal_mode=WAL` đã được thực thi khi khởi tạo kết nối chưa."

### 🎨 Linh (Frontend 1)
- **Branch:** `feature/linh/design-system`
- **Tasks:**
    - [ ] **Tokenize Design System:** Di chuyển toàn bộ màu sắc hardcoded trong CSS vào `.root` của `variables.css` (primary, success, danger, surface colors).
    - [ ] **Wallet Refinement:** Sử dụng `NumberFormat.getCurrencyInstance()` cho toàn bộ hiển thị tiền tệ và cải thiện Validation/Error messages.
    - [ ] **Empty States & Transitions:** Thêm các placeholder trực quan khi danh sách trống và mượt hóa hiệu ứng chuyển View.
- **Gemini Prompt:**
  > "Gemini, hãy rà soát `variables.css` và định nghĩa các looked-up colors chuẩn. Sau đó, thay thế các mã hex trong `common.css` và `auth.css` bằng các biến vừa định nghĩa. Cuối cùng, cập nhật `WalletController` để format tiền theo chuẩn USD."

### 🚀 Hải Anh (Frontend 2)
- **Branch:** `feature/haianh/realtime-polish`
- **Tasks:**
    - [ ] **Live UI Polish:** Cập nhật `live-bidding.css` theo Design System mới. Thêm `accessibleText` và tooltip cho toàn bộ icon điều khiển.
    - [ ] **Socket Resilience:** Xử lý sự kiện ngắt kết nối socket, hiển thị Banner cảnh báo "Disconnected" và cơ chế retry UI.
    - [ ] **Visual Feedback:** Thêm hiệu ứng Animation (ví dụ: Scale hoặc Color flash) khi giá tiền thay đổi realtime.
- **Gemini Prompt:**
  > "Gemini, hãy giúp tôi cập nhật `LiveBiddingView.fxml` để thêm Tooltip cho các nút Auto-bid. Sau đó, viết logic trong `LiveBiddingController` để khi nhận `BID_UPDATE`, nhãn giá tiền sẽ nhấp nháy màu xanh trong 0.5s để thu hút sự chú ý."

---

## 16. Archived Tasks (Sprint Week 14)
<details>
<summary>Nhấn để xem các task đã hoàn thành</summary>

#### 🔴 ƯU TIÊN 1: Hoàn thiện Logic Nghiệp vụ & Dashboard (Core)
- **Huy (Lead):**
    - [x] Task: `GET_MY_BIDS` API & Admin Logic.
    - [x] Task: Triển khai `GET_DASHBOARD` API (trả về stats thực tế cho từng role).
    - [x] Task: Triển khai Proxy Bidding chuyên nghiệp (Server-side) & Anti-sniping.
    - [x] Task: Logic đấu giá truyền thống (Manual Bid) kết hợp phản hồi Auto-bid.
- **Hải Anh:**
    - [x] Task: Auction Detail Data Binding & `NotificationManager`.
    - [x] Task: Hoàn thiện Admin Panel UI (Kết nối API `ADMIN_GET_USERS` và `ADMIN_UPDATE_USER_STATUS`).
    - [x] Task: Xử lý sự kiện realtime `AUCTION_CLOSED` và `TIME_EXTENDED` trên UI.

#### 🟡 ƯU TIÊN 2: Seller Features & UX Refinement
- **Linh:**
    - [x] Task: Filter/Search tại `AuctionList` & `MyBidsController` implementation.
    - [x] Task: Cài đặt và sử dụng **Ikonli** cho toàn bộ UI.
    - [x] Task: Cập nhật Seller Center: Hiển thị danh sách Auction của chính mình.
    - [x] Task: Chính xác hóa trạng thái "Winning/Outbid" trong My Bids dựa trên `highestBidderId`.
- **Mạnh:**
    - [x] Task: `findByBidderId` in DAO & Concurrency Stress Test.
    - [x] Task: Kích hoạt `WAL Mode` trong SQLite và tối ưu hóa Transaction.
    - [x] Task: Triển khai `DELETE_ITEM` (Soft delete) và ràng buộc nghiệp vụ.
    - [x] Task: Triển khai Escrow & Settlement (Trừ tiền khi thắng, hoàn tiền khi outbid/hủy).

#### 🔵 ƯU TIÊN 3: Polish & Final Test
- **Hải Anh:** [x] Tích hợp thông báo cá nhân hóa cho Seller và Toast realtime.
- **Mạnh:** [x] Bổ sung bộ JUnit E2E Test cho luồng tiền và đấu giá (`AuctionSettlementTest`).
- **Huy:** [x] Final Security & Performance Review trước khi demo.
</details>

---

## 17. Setup môi trường
- Java 21, Maven 3.9+, SQLite.
- Server: `mvn -pl server exec:java`
- Client: `mvn -pl client javafx:run`

## 18. GitHub Actions
Tự động chạy `mvn test` trên mỗi PR vào `dev` và `main`.

## 19. Test plan
- Unit tests cho Service và DAO.
- E2E Testing với nhiều client cùng lúc.

## 20. Definition of Done
- Build success, Pass Tests, Lead Approved, Merged to `dev`.

## 21. Checklist chấm điểm
- OOP, Design Patterns, Concurrency, Realtime, Client-Server, MVC.

## 22. Rủi ro và cách tránh
- SQLite Busy: Sử dụng WAL mode và Lock nghiệp vụ.
- JavaFX Threading: Luôn dùng `Platform.runLater()`.

## 23. Việc cần làm ngay (Action Items)
- Huy: Khai báo MessageType mới cho My Bids và Admin.
- Linh: Cập nhật UI Auction List để nhận sự kiện filter.

## 24. Ghi chú cuối
Tập trung vào luồng: **Register/Login -> Browse -> Bid -> Realtime Update -> Close.**

## 25. Tài liệu tham khảo
- `docs/protocol.md`
- `docs/class-diagram.md`
