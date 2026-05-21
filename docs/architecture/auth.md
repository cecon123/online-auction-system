# Xác thực & Phân quyền (Auth & Permissions)

Hệ thống AuctionPro triển khai cơ chế bảo mật dựa trên Token đơn giản qua kết nối Socket bền vững.

## 1. Luồng Đăng nhập (Login Flow)

1. **Client:** Gửi `Request` loại `LOGIN` chứa `username` và `password`.
2. **Server:** 
    - Truy vấn người dùng từ cơ sở dữ liệu.
    - So sánh mật khẩu bằng `BCrypt.checkpw`.
    - Nếu khớp, tạo một chuỗi `UUID` ngẫu nhiên làm **Session Token**.
    - Lưu token vào `SessionManager` kèm theo `userId` và thời điểm hết hạn phiên.
    - Trả về `LoginResponse` chứa token và thông tin vai trò (Role).
3. **Client:** Lưu token vào `SocketClient` để đính kèm vào tất cả các yêu cầu tiếp theo trong trường `token`.

## 2. Quản lý Vai trò (Role Handling)

Hệ thống có 3 vai trò chính định nghĩa trong enum `Role`:

- **BIDDER (Người đấu giá):**
    - Có quyền xem danh sách đấu giá, chi tiết và đặt giá (Place Bid).
    - Có quyền quản lý ví tiền cá nhân.
- **SELLER (Người bán):**
    - Có quyền tạo mặt hàng (`Item`) và mở các phiên đấu giá (`Auction`).
    - Có quyền quản lý các phiên đấu giá do mình sở hữu.
- **ADMIN (Quản trị viên):**
    - Có quyền truy cập Bảng điều khiển quản trị (Admin Panel).
    - Có quyền kích hoạt/khóa tài khoản người dùng khác.
    - Có quyền hủy bỏ bất kỳ phiên đấu giá nào trong hệ thống.

## 3. Kiểm tra Quyền (Permission Flow)

Mọi yêu cầu gửi tới Server (trừ Login/Register) đều đi qua bước kiểm tra tại `RequestRouter`:

1. Server trích xuất `token` từ `Request`.
2. Kiểm tra tính hợp lệ của token trong `SessionManager` để lấy `userId`; token hết hạn sẽ bị xóa khỏi bộ nhớ và bị coi là không hợp lệ.
3. Nếu không có token hợp lệ, trả về lỗi `Unauthorized`.
4. Với các yêu cầu quản trị (ví dụ: `ADMIN_GET_USERS`), server thực hiện thêm bước `requireAdmin(request)` để kiểm tra vai trò người dùng trong database có phải là `ADMIN` hay không.

## 4. Bảo mật Dữ liệu
- **Mật khẩu:** Luôn được băm bằng thuật toán **BCrypt** trước khi lưu vào SQLite, đảm bảo ngay cả khi lộ database, mật khẩu gốc vẫn an toàn.
- **Session:** Token phiên làm việc chỉ tồn tại trong bộ nhớ RAM của Server, có TTL 2 giờ, và sẽ bị xóa khi người dùng đăng xuất (`LOGOUT`), khi hết hạn, hoặc khi Server khởi động lại.
- **Logging:** Client chỉ log trạng thái token dạng `token=set` hoặc `token=none`, không ghi raw session token hay payload mật khẩu vào log debug.
