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

## 14. Git Workflow chuẩn (Team Rule)

### 14.1 Branching Strategy
- **`main`**: Ổn định, dùng để Demo. Không commit trực tiếp.
- **`dev`**: Tích hợp, mọi tính năng được merge vào đây trước qua PR.
- **`feature/<tên-người-làm>/<tên-task>`**: Branch làm việc cá nhân.
    *   Ví dụ: `feature/manh/sqlite-item-dao`

### 14.2 Quy trình làm việc hàng ngày
1. **Bắt đầu:** `git checkout dev` -> `git pull origin dev` -> `git checkout -b feature/<tên-người-làm>/<tên-task>`.
2. **Code & Local Test:** Code và chạy thử Server/Client (xem mục 17).
3. **Push:** `git push origin feature/<tên-người-làm>/<tên-task>`.
4. **Pull Request (PR):** Tạo PR trên GitHub vào branch `dev`.
5. **Review:** Gửi link PR cho **Huy (Lead)** để review. Sửa code nếu có comment.
6. **Merge:** Chỉ sau khi Huy **Approve**, branch mới được merge vào `dev`.
7. **Sync:** Toàn team pull `dev` về và chạy `mvn clean install` để đồng bộ.

---

## 15. Roadmap & Task Board (W7 - Priority Based)

Dự án hiện đang tập trung vào **Tuần 7: Concurrency & Realtime Foundation**. Các task được sắp xếp theo chuỗi phụ thuộc: Mạnh -> Huy -> Linh -> Hải Anh.

### 16. Task Board (Cập nhật tiến độ tại đây)

#### 🟢 ƯU TIÊN 1: Tầng Dữ liệu & Persistence (Mạnh)
*Huy và Linh cần DAO thật để làm Service và test socket.*
- [x] `UserDao` & `SQLiteUserDao` (Done - feature/manh/sqlite-user-dao)
- [x] Interface `ItemDao`, `AuctionDao`, `BidDao` (Done)
- [x] Triển khai `SQLiteItemDao`, `SQLiteAuctionDao`, `SQLiteBidDao` (Done)
- [x] Unit Test cho toàn bộ DAO (Done)

#### 🔵 ƯU TIÊN 2: Tầng Nghiệp vụ & Bảo mật (Huy)
*Linh cần AuthService thật để nối giao diện Login/Register.*
- [x] `AuctionLockManager` & `BidService` skeleton (Done - feature/huy/auction-locking)
- [x] `AuthService` với BCrypt hashing (Done - feature/huy/auth-service-security)
- [x] Hoàn thiện `BidService` logic với DAO thật (Done - feature/huy/bid-service-real-integration)
- [x] `AuctionService` (Quản lý trạng thái OPEN/RUNNING/FINISHED) (Done)

#### 🟡 ƯU TIÊN 3: Tầng Kết nối Socket (Linh)
*Hải Anh cần SocketClient để gửi/nhận dữ liệu thật từ các màn hình.*
- [x] `SocketClient` chạy nền (Background Thread) (Done - feature/linh/client-socket-foundation)
- [x] `AuthClientService` (Nối Login/Register với socket thật) (Done - feature/linh/client-socket-foundation)
- [x] Cập nhật UI Client không dùng mock logic (Done - feature/linh/client-socket-foundation)

#### 🟠 ƯU TIÊN 4: Tầng Tích hợp UI & Realtime (Hải Anh & Gemini)
- [x] Cải thiện UI Auction List (Scroll, Status Badge, Responsive Tiles) (Done)
- [x] Persistence: Giữ trạng thái phiên đấu giá đã chọn khi điều hướng (Done)
- [x] Fix lỗi Realtime: Xử lý `BID_UPDATE` và dispatch sự kiện chính xác (Done)
- [x] Đồng bộ số dư: Tự động cập nhật TopBar khi số dư thay đổi (Done)
- [x] Sửa lỗi Optimistic Locking khi đặt thầu (Done)
- [ ] Hoàn thiện logic lọc (Filter) và tìm kiếm (Search) tại Auction List (Next)
- [ ] Thông báo (Notification) Realtime khi phiên đấu giá kết thúc (Next)
- [ ] Tích hợp biểu đồ giá chi tiết và lịch sử thầu đầy đủ (Next)

---

## 17. Kế hoạch cho thành viên tiếp theo (Next Steps)

1.  **Hoàn thiện Filter/Search:** Hiện tại UI đã có thanh Search và ComboBox nhưng logic filter trong `AuctionListController` cần được kết nối với API/Backend.
2.  **Notification System:** Xây dựng cơ chế hiện Toast hoặc Alert khi người dùng bị outbid hoặc khi phiên đấu giá họ đang theo dõi kết thúc.
3.  **My Bids View:** Xây dựng màn hình danh sách các phiên đấu giá mà người dùng hiện tại đã tham gia đặt thầu.
4.  **Admin Panel:** Hoàn thiện giao diện quản lý người dùng và phiên đấu giá cho tài khoản Admin.

---

## 17. Checklist kiểm thử bắt buộc (Trước khi Commit/PR)
Hành động bắt buộc cho mọi thành viên (AI Agent phải nhắc nhở):
1.  **Build:** `mvn clean install` -> Đảm bảo không lỗi dependency.
2.  **Server:** `mvn -pl server exec:java` -> Server khởi động không crash.
3.  **Client:** `mvn -pl client javafx:run` -> Client mở được giao diện.
4.  **Test:** `mvn test` -> Tất cả unit test phải Pass.
5.  **Review:** Gửi mã nguồn cho **Huy** và chỉ merge khi được phê duyệt.

---

## 18. Coding conventions (Quy ước Code)

- Java class: `PascalCase`.
- Method/variable: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- DAO naming: `UserDao` / `SQLiteUserDao` (không dùng DAO viết hoa toàn bộ).
- Không để business logic trong JavaFX Controller.
- Tuyệt đối không để SQL query trong Service layer.
- Client tuyệt đối không import bất kỳ package nào từ module `server`.

---

## 19. Presentation notes (Ghi chú bảo vệ)

Khi bảo vệ BTL, mỗi thành viên cần nắm vững:
- **Huy:** Kiến trúc tổng thể, Socket Protocol, Concurrency (Locking), Security.
- **Mạnh:** SQLite Schema, DAO Pattern, Database Integrity.
- **Linh:** JavaFX MVC, Socket Client Threading.
- **Hải Anh:** Realtime UI Update, LineChart Visualization, Auto-bidding.

---

## 20. Definition of Done (Định nghĩa Hoàn thành)

Một task được coi là hoàn thành (Done) khi:
- Code build thành công và vượt qua các Unit Test.
- Tuân thủ đúng kiến trúc và quy ước code.
- Đã được Huy (Lead) review và Approve trên GitHub.
- Đã được merge vào branch `dev`.
- Tài liệu liên quan (nếu có) được cập nhật.
