# Giao thức Socket JSON (Socket JSON Protocol)

Tất cả các tin nhắn trao đổi giữa client và server là các chuỗi **JSON phân tách bằng ký tự xuống dòng** (newline-delimited JSON).

> **QUY TẮC QUAN TRỌNG:**
> Một yêu cầu (request) = một dòng JSON. Một phản hồi (response) = một dòng JSON.
> KHÔNG sử dụng định dạng "pretty printing" (JSON nhiều dòng) khi gửi qua socket, vì `ClientHandler` sử dụng phương thức `readLine()` để bóc tách các tin nhắn.

## Định dạng Yêu cầu (Request Format)

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

- `type`: Tên của enum `MessageType`.
- `requestId`: Một định danh duy nhất (UUID) do client tạo ra để theo dõi các phản hồi.
- `token`: Token phiên làm việc nhận được sau khi ĐĂNG NHẬP (LOGIN). Để trống đối với các yêu cầu xác thực (AUTH).
- `data`: Payload cụ thể theo từng loại yêu cầu.
  - Đối với `PLACE_BID`: Nếu phiên đấu giá đã có người thầu, mức giá mới phải ít nhất bằng **Giá hiện tại + 10.00$**.

## Định dạng Phản hồi (Response Format)

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
    "timestamp": "2026-05-10T20:30:00"
  }
}
```

- `success`: `true` nếu xử lý thành công, `false` nếu ngược lại.
- `message`: Thông báo trạng thái hoặc lỗi có thể đọc được bằng ngôn ngữ tự nhiên.

## Định dạng Sự kiện Thời gian thực (Realtime Event Format)

Các sự kiện do server chủ động đẩy xuống (push) sẽ có `requestId` là `null` hoặc một tiền tố sự kiện chung.

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
    "timestamp": "2026-05-10T20:30:00",
    "newEndTime": "2026-05-10T21:00:00"
  }
}
```

## Danh mục Loại Tin nhắn (Message Types)

| Phân loại | Loại tin nhắn (Type) | Mục đích |
|---|---|---|
| **XÁC THỰC** | `REGISTER` | Tạo tài khoản người dùng mới |
| | `LOGIN` | Xác thực và nhận token phiên làm việc |
| | `LOGOUT` | Hủy hiệu lực phiên làm việc |
| **DASHBOARD** | `GET_DASHBOARD` | Lấy thông tin tổng quan theo vai trò người dùng |
| **ĐẤU GIÁ** | `GET_AUCTIONS` | Liệt kê tất cả các phiên đấu giá đang/sắp diễn ra |
| | `GET_AUCTION_DETAIL` | Thông tin chi tiết của một phiên đấu giá |
| | `CREATE_AUCTION` | Người bán: Đăng phiên đấu giá mới |
| | `UPDATE_AUCTION` | Người bán: Chỉnh sửa trước khi bắt đầu (RUNNING) |
| | `CANCEL_AUCTION` | Người bán/Quản trị viên: Dừng phiên đấu giá |
| | `GET_SELLER_AUCTIONS` | Người bán: Xem danh sách phiên sở hữu |
| | `GET_SELLER_STATS` | Người bán: Xem thống kê doanh thu và lượt thầu |
| **MẶT HÀNG** | `CREATE_ITEM` | Người bán: Đăng ký mặt hàng mới |
| | `UPDATE_ITEM` | Người bán: Chỉnh sửa thông tin mặt hàng |
| | `DELETE_ITEM` | Người bán: Xóa mặt hàng |
| **ĐẶT GIÁ** | `PLACE_BID` | Người đấu giá: Đặt một mức giá thầu mới |
| | `GET_BID_HISTORY` | Xem lịch sử thầu của một phiên đấu giá |
| | `GET_MY_BIDS` | Liệt kê các phiên đấu giá người dùng đã tham gia |
| | `GET_USER_BID_HISTORY` | Lịch sử tham gia chi tiết (Thắng/Đang dẫn đầu/Bị vượt giá) |
| **REALTIME** | `SUBSCRIBE_AUCTION` | Đăng ký lắng nghe cập nhật của một phiên |
| | `UNSUBSCRIBE_AUCTION` | Ngừng lắng nghe cập nhật |
| | `BID_UPDATE` | Sự kiện Server: Phát sóng lượt thầu mới |
| | `AUCTION_CLOSED` | Sự kiện Server: Phát sóng kết thúc phiên |
| | `TIME_EXTENDED` | Sự kiện Server: Phát sóng gia hạn thời gian (Anti-sniping) |
| | `AUCTION_LIST_UPDATED`| Sự kiện Server: Danh sách chung thay đổi |
| **VÍ TIỀN** | `DEPOSIT` | Nạp tiền vào số dư người dùng |
| | `WITHDRAW` | Rút tiền từ số dư người dùng |
| **TỰ ĐỘNG THẦU** | `SET_AUTO_BID` | Cấu hình mức thầu tối đa và bước giá |
| | `GET_AUTO_BID` | Kiểm tra luật tự động thầu hiện tại của phiên |
| **QUẢN TRỊ** | `ADMIN_GET_USERS` | Danh sách tất cả người dùng |
| | `ADMIN_UPDATE_USER_STATUS` | Kích hoạt/Vô hiệu hóa người dùng |
| | `ADMIN_GET_AUCTIONS` | Quản lý toàn bộ phiên đấu giá hệ thống |
| | `ADMIN_CANCEL_AUCTION` | Dừng phiên đấu giá cưỡng bức bởi Admin |
| | `SYSTEM_NOTIFICATION` | Tin nhắn trực tiếp từ server tới người dùng |
