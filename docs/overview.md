# Tổng quan Dự án (Project Overview)

## 1. Mục tiêu Hệ thống
AuctionPro là một nền tảng đấu giá trực tuyến (Online Auction System) được thiết kế để cung cấp trải nghiệm đấu giá an toàn, minh bạch và thời gian thực. Hệ thống hỗ trợ người dùng đăng ký mặt hàng, tham gia đấu giá trực tiếp và quản lý các giao dịch thông qua ví điện tử tích hợp.

## 2. Các tính năng chính
- **Đấu giá thời gian thực:** Cập nhật giá thầu và trạng thái phiên đấu giá ngay lập tức cho tất cả người dùng tham gia.
- **Tự động đấu giá (Proxy Bidding):** Cho phép người dùng đặt mức giá tối đa, hệ thống sẽ tự động đặt giá thay người dùng với bước giá thấp nhất.
- **Quản lý đa vai trò:** Phân quyền rõ ràng giữa Người mua (Bidder), Người bán (Seller) và Quản trị viên (Admin).
- **Ví điện tử:** Quản lý số dư, phong tỏa tiền khi đang dẫn đầu thầu để đảm bảo thanh toán.

## 3. Kiến trúc Tổng quan
Hệ thống sử dụng kiến trúc **Client-Server** truyền thống dựa trên giao thức TCP Socket tùy chỉnh:
- **Server:** Chịu trách nhiệm xử lý logic nghiệp vụ, quản lý trạng thái phiên đấu giá và lưu trữ dữ liệu vào SQLite.
- **Client:** Ứng dụng desktop JavaFX cung cấp giao diện tương tác người dùng, kết nối tới server qua Socket.
- **Protocol:** Giao thức JSON phân tách bằng dòng (Newline-Delimited JSON) để đảm bảo tính đơn giản và tốc độ truyền tải.

## 4. Công nghệ sử dụng
- **Ngôn ngữ:** Java 21 (JDK 21).
- **Phía Server:**
    - Socket Programming (TCP/IP).
    - SQLite (Database).
    - SLF4J + Logback (Logging).
    - BCrypt (Password Hashing).
    - Gson (JSON Serialization).
- **Phía Client:**
    - JavaFX (UI Framework).
    - Ikonli (Icon system).
    - Maven (Build tool).
