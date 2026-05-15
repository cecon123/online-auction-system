# Khắc phục Sự cố (Troubleshooting)

Tài liệu này tổng hợp các vấn đề thường gặp và cách xử lý trong quá trình phát triển và vận hành AuctionPro.

## 1. Vấn đề về Kết nối Socket

### Hiện tượng: Client hiển thị "Connection Refused" ngay khi khởi động.
- **Nguyên nhân:** Server chưa được bật hoặc đang chạy trên một cổng (Port) khác.
- **Cách xử lý:** 
    - Kiểm tra xem bạn đã chạy `mvn -pl server exec:java` chưa.
    - Đảm bảo cổng `8080` không bị chiếm dụng bởi ứng dụng khác.

### Hiện tượng: Kết nối bị ngắt quãng liên tục.
- **Nguyên nhân:** Tường lửa (Firewall) hoặc phần mềm diệt virus chặn kết nối TCP cục bộ.
- **Cách xử lý:** Thêm ngoại lệ cho ứng dụng Java hoặc tạm tắt tường lửa để kiểm tra.

## 2. Vấn đề về Biên dịch (Build Issues)

### Hiện tượng: Lỗi "Class not found" sau khi cập nhật code từ Git.
- **Nguyên nhân:** Các module chưa được đồng bộ hóa bản build mới nhất.
- **Cách xử lý:** Chạy `mvn clean install` ở thư mục gốc của dự án để đảm bảo module `common` được cài đặt vào kho lưu trữ cục bộ trước khi `server` và `client` biên dịch.

### Hiện tượng: Lỗi JavaFX không khởi động được (Graphics Device error).
- **Nguyên nhân:** Thiếu driver đồ họa hoặc đang chạy trong môi trường không hỗ trợ giao diện (ví dụ: WSL không cấu hình X11).
- **Cách xử lý:** Đảm bảo bạn đang chạy ứng dụng trực tiếp trên hệ điều hành có hỗ trợ đồ họa (Windows/macOS/Linux Desktop).

## 3. Vấn đề về Dữ liệu (Database Issues)

### Hiện tượng: Lỗi "Database is locked".
- **Nguyên nhân:** Có nhiều ứng dụng đang cùng ghi vào file `auction.db` hoặc một transaction chưa được đóng đúng cách.
- **Cách xử lý:** 
    - Đảm bảo chỉ có một tiến trình Server đang chạy.
    - Sử dụng một công cụ quản lý SQLite (như DB Browser for SQLite) để kiểm tra xem có tiến trình nào đang giữ khóa không.

### Hiện tượng: Ảnh không hiển thị trên giao diện.
- **Nguyên nhân:** Đường dẫn trong `uploads/` không khớp hoặc Server chưa phục vụ tài nguyên tĩnh đúng cách.
- **Cách xử lý:** Kiểm tra xem ảnh có tồn tại trong thư mục `uploads/` của module server không. Xem log backend để biết URL của ảnh đang được sinh ra như thế nào.
