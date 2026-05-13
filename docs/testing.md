# Hướng dẫn Kiểm thử (Testing Guide)

Hệ thống AuctionPro được kiểm thử thông qua sự kết hợp giữa các bài kiểm tra tự động và quy trình thủ công nghiêm ngặt.

## 1. Kiểm thử Tự động (Automated Testing)

Dự án sử dụng JUnit 5 và Mockito cho các bài test.
```bash
mvn test
```

### 1.1 Unit Tests
- Tập trung vào logic nghiệp vụ tại các lớp Service (`BidServiceTest`, `AuctionServiceTest`).
- Giả lập (Mock) các lớp DAO để kiểm soát dữ liệu đầu vào và kiểm tra các trường hợp biên (số dư không đủ, thời gian không hợp lệ).

### 1.2 Integration Tests
- Kiểm tra sự tương tác giữa code và cơ sở dữ liệu SQLite thực tế.
- Đảm bảo các ràng buộc (Constraints) trong DB hoạt động đúng như mong đợi.

## 2. Kiểm thử Thủ công (Manual Testing)

### 2.1 Kiểm thử Luồng Realtime
Đây là phần quan trọng nhất để đảm bảo Socket hoạt động ổn định:
1. Mở 3 cửa sổ Client đồng thời.
2. **Client A & B:** Cùng vào một phòng đấu giá trực tiếp.
3. **Client C:** Đăng nhập với vai trò Admin.
4. Client A thực hiện đặt thầu.
5. **Xác nhận:** Client B thấy giá cập nhật ngay lập tức mà không cần load lại. Client C (Admin) thấy số liệu thống kê trong Admin Panel thay đổi tức thì.

### 2.2 Kiểm thử Kết nối (Socket Resilience)
1. Đang mở Client và đã đăng nhập.
2. Tắt ứng dụng Server (Ctrl+C).
3. **Xác nhận:** Client hiển thị thông báo "Reconnecting" hoặc đổi màu trạng thái kết nối trên thanh công cụ.
4. Bật lại Server.
5. **Xác nhận:** Client tự động kết nối lại và thực hiện "Silent Re-auth" để tiếp tục phiên làm việc mà không yêu cầu người dùng nhập lại mật khẩu.

## 3. Danh sách kiểm tra (Regression Checklist)
Trước mỗi lần bàn giao, hãy kiểm tra các mục sau:
- [ ] Đăng ký/Đăng nhập thành công với cả 3 vai trò.
- [ ] Người bán có thể tải ảnh lên và tạo phiên đấu giá.
- [ ] Người thầu có thể nạp tiền và đặt thầu.
- [ ] Hệ thống tự động đóng phiên đấu giá khi hết thời gian.
- [ ] Admin có thể khóa tài khoản người dùng vi phạm.
- [ ] Logic phong tỏa số dư (Locked Balance) hoạt động chính xác.
