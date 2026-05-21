# Kiến trúc Realtime (Thời gian thực)

Tài liệu này mô tả cách các tính năng thời gian thực (realtime) được triển khai trong AuctionPro bằng cách sử dụng giao tiếp Socket tùy chỉnh.

## 1. Tổng quan

AuctionPro sử dụng giao thức TCP Socket tùy chỉnh thay vì HTTP/WebSockets để minh họa lập trình mạng cấp thấp. Giao tiếp là hai chiều (bi-directional) và bất đồng bộ (asynchronous).

### Các thành phần
- **Server:** Lắng nghe trên cổng 8080. Mỗi kết nối client được xử lý bởi một luồng `ClientHandler` riêng biệt.
- **Client:** Sử dụng một `SocketClient` duy nhất (singleton) để quản lý kết nối. Một luồng `SocketListener` chạy ngầm để đọc các tin nhắn từ server.
- **Giao thức (Protocol):** Các tin nhắn được trao đổi dưới dạng đối tượng JSON trên một dòng (Newline-delimited JSON).

## 2. Luồng giao thức

### Yêu cầu - Phản hồi (Bất đồng bộ)
1. Client tạo một `requestId` duy nhất (UUID).
2. Client gửi đối tượng `Request` tới server.
3. Server xử lý yêu cầu và trả về một `Response` với CÙNG một `requestId`.
4. `SocketClient` sử dụng một `Map<String, CompletableFuture>` để khớp phản hồi với yêu cầu ban đầu và hoàn tất future.

### Đẩy dữ liệu từ Server (Realtime Events)
1. Server kích hoạt một sự kiện (ví dụ: `BID_UPDATE`, `AUCTION_LIST_UPDATED`).
2. `NotificationService` xác định các client mục tiêu (những người đăng ký hoặc tất cả người dùng).
3. Server gửi một đối tượng `Response` với `requestId` có tiền tố là `event-` hoặc `sys-notify-`.
4. `SocketClient` nhận diện đây là các sự kiện và chuyển tiếp chúng tới các `eventListeners` đã đăng ký.

## 3. Xử lý đa luồng phía Client

Tất cả các hoạt động socket diễn ra trên các luồng nền:
- **Gửi dữ liệu:** `sendRequest` thực thi trên luồng của người gọi nhưng không gây nghẽn (trả về `CompletableFuture`).
- **Nhận dữ liệu:** Luồng `SocketListener` đọc dữ liệu từ luồng vào (input stream).
- **Cập nhật giao diện:** Để cập nhật UI, tất cả các trình lắng nghe sự kiện (event listeners) trong `SocketClient` được bao bọc trong `Platform.runLater()` để đảm bảo chúng thực thi trên **JavaFX Application Thread**.

## 4. Trạng thái mất kết nối hiện tại

Code hiện tại chưa triển khai retry reconnect hoặc silent re-authentication.

Khi server đóng kết nối hoặc socket bị lỗi, `SocketClient`:
1. Dừng listener socket.
2. Chuyển `ConnectionState` về `DISCONNECTED`.
3. Fail các request đang chờ trong `pendingRequests`.
4. Xóa token phiên ở client.

Enum `ConnectionState.RECONNECTING` vẫn tồn tại để dành cho mở rộng sau, nhưng luồng hiện tại không set trạng thái này.

## 5. Các loại thông điệp sự kiện

| MessageType | Mô tả | Phạm vi |
|-------------|-------|---------|
| `BID_UPDATE` | Có lượt đặt giá mới cho một cuộc đấu giá | Người đăng ký đấu giá đó |
| `AUCTION_LIST_UPDATED` | Cuộc đấu giá được tạo, hủy hoặc thay đổi trạng thái | Toàn bộ hệ thống |
| `USER_LIST_UPDATED` | Người dùng mới đăng ký hoặc cập nhật trạng thái | Toàn bộ hệ thống (Admin) |
| `SYSTEM_NOTIFICATION` | Tin nhắn trực tiếp tới một người dùng cụ thể | Người dùng mục tiêu |
