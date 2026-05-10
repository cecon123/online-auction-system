# Hướng dẫn Sử dụng - AuctionPro

Tài liệu này hướng dẫn bạn các tính năng và quy trình hoạt động của hệ thống AuctionPro.

## 1. Bắt đầu

### 1.1 Yêu cầu Hệ thống
- Java 21 trở lên.
- Maven 3.9+.
- SQLite (trình điều khiển tích hợp sẵn).

### 1.2 Khởi chạy Hệ thống
1.  **Khởi động Server**:
    ```bash
    mvn -pl server exec:java
    ```
2.  **Khởi động Client**:
    ```bash
    mvn -pl client javafx:run
    ```

---

## 2. Quy trình Nghiệp vụ Thông thường

### 2.1 Đối với Người thầu (Bidder)
1.  **Đăng ký**: Tạo tài khoản với vai trò `BIDDER`.
2.  **Bảng điều khiển**: Duyệt danh sách các phiên đấu giá đang "Running" (Diễn ra) tại màn hình chính.
3.  **Đấu giá Trực tiếp**: 
    - Nhấp vào thẻ phiên đấu giá để vào phòng thầu trực tiếp.
    - Xem **Biểu đồ Đường giá** thời gian thực.
    - **Đặt giá thủ công**: Nhập số tiền cao hơn giá hiện tại (ít nhất là bước giá quy định).
    - **Tự động thầu (Proxy)**: Nhập ngân sách tối đa của bạn. Hệ thống sẽ tự động vượt mặt người khác bằng bước giá tối thiểu cho đến khi chạm tới ngân sách của bạn.
4.  **Ví tiền**: Nạp tiền vào số dư trong phần Wallet. Tiền của bạn sẽ bị **phong tỏa (locked)** trong khi bạn đang là người dẫn đầu phiên thầu.
5.  **Lịch sử thầu**: Theo dõi các phiên bạn đang dẫn đầu, đã bị vượt giá, hoặc đã thắng.

### 2.2 Đối với Người bán (Seller)
1.  **Quản lý Mặt hàng**: Truy cập **Seller Center** để đăng ký các mặt hàng (Điện tử, Nghệ thuật hoặc Xe cộ).
2.  **Tạo phiên đấu giá**: 
    - Chọn một mặt hàng và thiết lập giá khởi điểm, thời gian kết thúc.
    - Sau khi tạo, phiên đấu giá ở trạng thái `OPEN` và chuyển sang `RUNNING` khi tới giờ bắt đầu.
3.  **Theo dõi Doanh thu**: Xem thống kê các phiên thành công trong bảng thống kê của Seller.

### 2.3 Đối với Quản trị viên (Admin)
1.  **Quản lý Người dùng**: Kích hoạt hoặc vô hiệu hóa tài khoản người dùng.
2.  **Giám sát Hệ thống**: Xem tất cả các phiên đấu giá trên toàn hệ thống và hủy bỏ các phiên có dấu hiệu gian lận nếu cần thiết.

---

## 3. Các Tính năng Thời gian thực Nổi bật

### 3.1 Chống "Bắn tỉa" (Anti-Sniping)
Nếu có một lệnh thầu được đặt trong **30 giây cuối cùng** của phiên đấu giá, thời gian kết thúc sẽ tự động được gia hạn thêm **60 giây**. Điều này đảm bảo sự cạnh tranh công bằng và ngăn chặn việc dùng bot bắn tỉa giây cuối.

### 3.2 Cập nhật Trực tiếp
- Tất cả các màn hình đều cập nhật thời gian thực thông qua thông báo từ Socket. 
- Bạn không cần tải lại trang; các mặt hàng mới và thay đổi giá sẽ xuất hiện ngay lập tức.

### 3.3 Ký quỹ Ví (Wallet Escrow)
Hệ thống đóng vai trò là bên trung gian ký quỹ. Khi bạn thầu 1000$, số tiền đó sẽ được chuyển vào "Locked Balance". Nếu bạn thua phiên, tiền sẽ được hoàn trả ngay lập tức. Nếu bạn thắng, tiền sẽ được chuyển cho người bán khi phiên đấu giá kết thúc thành công.
