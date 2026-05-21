# Quản lý Trạng thái (State Management)

Dự án AuctionPro sử dụng các cơ chế quản lý trạng thái tập trung để đảm bảo tính nhất quán giữa Client và Server.

## 1. Trạng thái phía Client (Client-side State)

Phía Client sử dụng lớp `SceneManager` và `SocketClient` làm kho lưu trữ trạng thái trung tâm:

- **Session State:** Được lưu trong `SceneManager`, bao gồm:
    - `currentUserId`, `currentRole`, `currentUsername`.
    - `currentBalance`, `currentLockedBalance`.
    - Các thành phần UI đăng ký listener (`balanceListeners`) để tự động cập nhật số dư hiển thị khi có thay đổi.
- **Connection State:** Được lưu trong `SocketClient` bằng `ObjectProperty<ConnectionState>` (JavaFX property).
    - Các trạng thái: `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `RECONNECTING`.
    - Luồng hiện tại chỉ tự động dùng `DISCONNECTED`, `CONNECTING`, `CONNECTED`. `RECONNECTING` đang là trạng thái dự phòng; code chưa có retry reconnect/silent re-auth.
    - UI có thể "bind" trực tiếp vào thuộc tính này để hiển thị trạng thái kết nối lên thanh trạng thái (TopBar).
- **Navigation State:** `SceneManager` quản lý việc chuyển đổi giữa các FXML và duy trì `contentRoot` của ứng dụng.

## 2. Trạng thái phía Server (Server-side State)

Server duy trì trạng thái phiên làm việc trong bộ nhớ RAM:

- **Session Mapping:** `SessionManager` lưu trữ bản đồ giữa `Token -> Session(userId, expiresAt)`, với TTL 2 giờ cho mỗi token. Token hết hạn bị xóa khi server kiểm tra `getUserId(...)`; `LOGOUT` cũng invalidate token ngay lập tức.
- **Connection Mapping:** `NotificationService` lưu trữ bản đồ giữa `UserId -> Set<PrintWriter>` để biết client nào đang kết nối và gửi thông báo mục tiêu.
- **Subscription State:** `NotificationService` quản lý danh sách các client đã đăng ký (`SUBSCRIBE`) theo từng `AuctionId` để gửi cập nhật giá thầu realtime.

## 3. Luồng Đồng bộ hóa dữ liệu (Data Flow)

Hệ thống tuân thủ mô hình **Single Source of Truth** (Nguồn dữ liệu duy nhất):

1. Mọi thay đổi trạng thái quan trọng (Số dư, Giá thầu, Trạng thái đấu giá) đều phải bắt đầu từ Server.
2. Sau khi lưu vào Database thành công, Server sẽ đẩy thông tin mới xuống Client qua sự kiện Realtime.
3. Client nhận sự kiện, cập nhật các biến trong `SceneManager` hoặc gọi lại API để làm mới giao diện.
4. UI luôn được cập nhật thông qua cơ chế phản ứng (Reactive) dựa trên các thuộc tính của JavaFX hoặc các Listener.
