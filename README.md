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

### 🛡️ Huy (Lead)
- **Branch:** `feature/huy/final-audit-demo`
- **Tasks:**
    - [ ] Review & Merge toàn bộ PR từ `dev` vào `main`.
    - [ ] Xây dựng Demo Script (Kịch bản trình diễn các tính năng core).
    - [ ] Kiểm tra bảo mật cuối (BCrypt, Session, SQL Injection).
    - [ ] Tổng hợp báo cáo kết quả kiểm thử hệ thống.
- **Gemini Prompt:**
  > "Gemini, hãy giúp tôi rà soát toàn bộ code trên branch dev, kiểm tra các lỗ hổng bảo mật tiềm ẩn và chuẩn bị kịch bản demo (markdown) bao gồm các bước từ đăng ký đến kết thúc đấu giá."

### ⚙️ Mạnh (Backend)
- **Branch:** `feature/manh/perf-docs`
- **Tasks:**
    - [ ] Hoàn thiện JavaDoc cho toàn bộ các Service và DAO quan trọng.
    - [ ] Tạo script/data mẫu (seed.sql) để demo nhanh các trạng thái đấu giá khác nhau.
    - [ ] Kiểm tra lần cuối các edge case về Concurrency (Deadlock, Race condition).
    - [ ] Tối ưu hóa log hệ thống để dễ dàng theo dõi trong buổi demo.
- **Gemini Prompt:**
  > "Gemini, hãy hoàn thiện JavaDoc cho các file trong gói service và dao. Sau đó, cập nhật file `seed.sql` với dữ liệu phong phú để demo, bao gồm các auction sắp kết thúc và các auction đã hoàn thành."

### 🎨 Linh (Frontend 1)
- **Branch:** `feature/linh/ux-refinement`
- **Tasks:**
    - [ ] Tinh chỉnh hiệu ứng chuyển cảnh (Transitions) giữa các View.
    - [ ] Cải thiện thông báo lỗi trên UI (Error dialogs/labels) tại Login/Register.
    - [ ] Đảm bảo UI thống nhất về màu sắc và font chữ dựa trên `variables.css`.
    - [ ] Xử lý "Empty State" (Khi danh sách auction hoặc bid trống).
- **Gemini Prompt:**
  > "Gemini, hãy rà soát các Controller JavaFX, thêm hiệu ứng Fade transition khi chuyển scene. Sau đó, cải thiện logic validation tại RegisterController để hiển thị thông báo lỗi chi tiết hơn trên UI."

### 🚀 Hải Anh (Frontend 2)
- **Branch:** `feature/haianh/realtime-viz`
- **Tasks:**
    - [ ] Hoàn thiện biểu đồ biến động giá (Real-time Line Chart) trong Auction Detail.
    - [ ] Tối ưu hệ thống Toast notification (Vị trí, màu sắc theo loại thông báo).
    - [ ] Thêm các widget trực quan (Charts/Stats) vào Admin Dashboard.
    - [ ] Chỉnh sửa giao diện Live Bidding để hiển thị danh sách người bid gần nhất sinh động hơn.
- **Gemini Prompt:**
  > "Gemini, hãy giúp tôi hoàn thiện `LiveBiddingController` để tích hợp `LineChart` hiển thị lịch sử đặt giá. Sau đó, tinh chỉnh `NotificationManager` để các Toast thông báo thắng cuộc có màu sắc nổi bật (Success theme)."

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
