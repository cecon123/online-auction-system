# Hướng dẫn Quản trị viên (Admin Panel)

Bảng điều khiển Admin cung cấp cho quản trị viên hệ thống các công cụ để theo dõi và quản lý nền tảng AuctionPro.

## 1. Truy cập Bảng điều khiển Admin

Đăng nhập bằng tài khoản có vai trò `ADMIN`.
- **Username mặc định:** `admin`
- **Password mặc định:** `admin123`

## 2. Các tính năng

### Thống kê Dashboard
Các bộ đếm thời gian thực cho:
- **Tổng số người dùng (Total Users):** Tổng số tất cả Bidder, Seller và Admin đã đăng ký.
- **Tổng số cuộc đấu giá (Total Auctions):** Số lượng tất cả các cuộc đấu giá đã từng được tạo.
- **Đang diễn ra (Running):** Số lượng các cuộc đấu giá hiện đang ở trạng thái `RUNNING`.
- **Đã kết thúc (Finished):** Số lượng các cuộc đấu giá đã hoàn thành.

### Quản lý người dùng
- Xem danh sách chi tiết tất cả người dùng, bao gồm vai trò và ngày tham gia.
- **Huy hiệu trạng thái:** `Active` (Hoạt động) hoặc `Suspended` (Bị khóa).
- **Các hành động quản trị:**
    - **Disable (Vô hiệu hóa):** Khóa tài khoản người dùng để ngăn họ đăng nhập.
    - **Enable (Kích hoạt):** Khôi phục tài khoản đã bị khóa.
    - *Lưu ý: Các tài khoản quản trị viên không thể bị vô hiệu hóa.*

### Quản lý đấu giá
- Theo dõi tất cả các cuộc đấu giá trên toàn nền tảng.
- **Thông tin chi tiết:** Danh mục, ID người bán, Giá khởi điểm và Giá hiện tại.
- **Huy hiệu trạng thái:** `OPEN`, `RUNNING`, `FINISHED`, `CANCELLED`.
- **Các hành động quản trị:**
    - **Cancel (Hủy):** Buộc hủy một cuộc đấu giá đang hoạt động (trạng thái phải là `OPEN` hoặc `RUNNING`). Đây là hành động không thể hoàn tác.

## 3. Cập nhật thời gian thực (Real-time)

Bảng điều khiển Admin có tính tương tác cao. Bạn không cần phải tải lại trang để thấy các thay đổi. Các sự kiện sau sẽ kích hoạt cập nhật UI tự động:
- **Đăng ký người dùng:** Người dùng mới xuất hiện trong danh sách ngay lập tức.
- **Thay đổi trạng thái người dùng:** Cập nhật được phản ánh trên tất cả các client Admin đang mở.
- **Tạo cuộc đấu giá:** Các cuộc đấu giá mới xuất hiện ngay trong bảng quản lý.
- **Thay đổi trạng thái đấu giá:** Huy hiệu trạng thái và giá đấu thầu được cập nhật theo thời gian thực.

## 4. Quy trình kiểm thử (Testing)

Để xác minh chức năng Admin:
1. Mở hai cửa sổ ứng dụng Client.
2. Đăng nhập vào **Client A** với tài khoản `admin`.
3. Đăng nhập vào **Client B** với một tài khoản `seller` mới.
4. Tạo một cuộc đấu giá trong **Client B**.
5. Quan sát **Client A**: Cuộc đấu giá mới sẽ xuất hiện ngay lập tức trong phần "Auction Management".
6. Tại **Client A**, nhấn "Cancel" cuộc đấu giá đó.
7. Quan sát **Client B**: Trạng thái đấu giá sẽ cập nhật thành `CANCELLED`.
