# Database & Luồng Backend (Backend Flow)

Tài liệu này chi tiết về cấu trúc lưu trữ và quy trình xử lý dữ liệu tại phía Server của AuctionPro.

## 1. Mô hình Dữ liệu (Models & Schema)

Hệ thống sử dụng SQLite với các bảng chính sau:

- **Users:** Lưu trữ thông tin định danh, mật khẩu đã băm, vai trò và số dư ví.
- **Items:** Thông tin về các mặt hàng cần đấu giá (tên, mô tả, loại, ảnh).
- **Auctions:** Quản lý các phiên đấu giá, bao gồm giá hiện tại, giá khởi điểm, giá bảo lưu (reserve price), thời gian bắt đầu/kết thúc và ID người dẫn đầu.
- **Bids:** Lưu trữ lịch sử tất cả các lượt đặt giá.
- **AutoBidRules:** Lưu trữ cấu hình thầu tự động cho từng người dùng trên từng phiên đấu giá.

## 2. Quy trình xử lý Service (Service Flow)

Mọi hành động nghiệp vụ đều tuân theo quy trình 3 bước:

### Ví dụ: Quy trình Đặt thầu (Place Bid)
1. **Validation:** `BidService` kiểm tra xem người dùng có đủ số dư không, phiên đấu giá có đang diễn ra (`RUNNING`) không, và mức giá mới có cao hơn mức hiện tại cộng với bước giá tối thiểu không.
2. **Persistence (SQLite Transaction):**
    - Cập nhật bản ghi `Auction` với `currentPrice` và `highestBidderId` mới.
    - Tạo một bản ghi mới trong bảng `Bids`.
    - Điều chỉnh số dư ví của người dùng (chuyển tiền từ `balance` sang `lockedBalance`).
    - Hoàn trả tiền phong tỏa cho người bị vượt mặt (nếu có).
3. **Notification:** Gọi `NotificationService` để broadcast sự kiện `BID_UPDATE` tới tất cả các client đang theo dõi phiên đấu giá đó.

## 3. Quản lý Đồng thời (Concurrency Management)

Hệ thống áp dụng các kỹ thuật để xử lý hàng ngàn lượt bid đồng thời:

- **AuctionLockManager:** Sử dụng cơ chế khóa theo `auctionId` để đảm bảo tại một thời điểm chỉ có một luồng xử lý bid cho một phiên đấu giá cụ thể, tránh tình trạng "race condition" (ghi đè giá thầu).
- **Idempotency Manager:** Ngăn chặn việc xử lý lặp lại cùng một yêu cầu (Anti-Replay) thông qua kiểm tra `requestId`.
- **SQLite Optimistic Locking:** Tận dụng cơ chế transaction của SQLite để đảm bảo tính toàn vẹn ACID của dữ liệu.

## 4. Tự động hóa (Background Tasks)

Backend chạy các luồng ngầm (`ScheduledExecutorService`) để:
- Kiểm tra các phiên đấu giá đã đến giờ bắt đầu (`OPEN` -> `RUNNING`).
- Kiểm tra và đóng các phiên đấu giá đã hết giờ (`RUNNING` -> `FINISHED`).
- Xử lý việc chuyển giao tài sản và thanh toán cuối cùng khi phiên thầu kết thúc thành công.
