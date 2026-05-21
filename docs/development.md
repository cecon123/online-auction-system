# Hướng dẫn Phát triển Dự án (Development Guide)

Tài liệu này cung cấp cái nhìn toàn diện về cấu trúc dự án, các quy trình kỹ thuật và hướng dẫn dành cho thành viên phát triển từ khi bắt đầu đến khi hoàn thiện.

## 1. Cấu trúc Dự án (Project Structure)

Dự án được tổ chức theo mô hình Maven Multi-module:

- `common/`: Chứa mã nguồn dùng chung cho cả Client và Server.
    - `dto/`: Các Data Transfer Objects để trao đổi dữ liệu qua Socket.
    - `protocol/`: Định nghĩa giao thức và các loại thông điệp (`MessageType`).
    - `model/`: Các thực thể domain cơ bản.
- `server/`: Xử lý logic nghiệp vụ và lưu trữ dữ liệu.
    - `dao/`: Lớp truy cập dữ liệu sử dụng SQLite.
    - `service/`: Lớp xử lý nghiệp vụ chính.
    - `exception/`: Custom exceptions cho lỗi nghiệp vụ/auth/wallet/bid.
    - `socket/`: Quản lý kết nối TCP và điều phối yêu cầu (`RequestRouter`).
- `client/`: Giao diện người dùng JavaFX.
    - `controller/`: Điều khiển logic cho các màn hình FXML.
    - `service/`: Proxy gọi API tới Server qua Socket.
    - `socket/`: Quản lý kết nối Client và lắng nghe sự kiện thời gian thực.

## 2. Luồng Xử lý Dữ liệu (Data Flow)

### 2.1 Yêu cầu API (Request Flow)
1. `Controller` gọi một phương thức trong `ClientService`.
2. `ClientService` tạo đối tượng `Request` và gửi qua `SocketClient`.
3. `SocketClient` đăng ký một `CompletableFuture` với `requestId` duy nhất.
4. Server nhận dòng JSON, `RequestRouter` giải mã và gọi `Service` tương ứng.
5. Server trả về `Response` kèm theo `requestId` ban đầu.
6. `SocketClient` khớp `requestId` và trả về kết quả cho `Controller`.

### 2.2 Sự kiện Thời gian thực (Realtime Flow)
1. Một hành động diễn ra trên Server (ví dụ: đặt thầu thành công).
2. `NotificationService` trên Server phát sóng (broadcast) sự kiện tới các Client.
3. `SocketClient` của Client nhận thông điệp, kiểm tra loại sự kiện.
4. Sự kiện được đẩy vào `Platform.runLater()` để cập nhật UI an toàn trên JavaFX thread.

## 3. Quản lý Trạng thái (State Management)

- **Client-side:** Sử dụng `SceneManager` để lưu trữ thông tin phiên làm việc hiện tại (UserId, Role, Balance) và điều hướng giữa các màn hình.
- **Server-side:** Sử dụng `SessionManager` để quản lý token và xác thực quyền hạn của mỗi kết nối.

## 4. Kiểm thử (Testing)

- **Unit Test:** Đặt tại `src/test/java` của từng module. Sử dụng JUnit 5 và Mockito.
- **Kiểm thử thủ công:**
    1. Chạy `mvn clean install` để build toàn bộ.
    2. Chạy Server: `mvn -pl server exec:java`.
    3. Chạy Client: `mvn -pl client javafx:run`.
    4. Sử dụng các tài khoản seed `admin`, `seller01`, `seller02`, `bidder01`, `bidder02` với password `123456` để kiểm tra flow demo.

## 5. Xử lý Lỗi & Logging

- **Logging:** Sử dụng SLF4J với Logback. File cấu hình tại `server/src/main/resources/logback.xml`.
- **Exception:** Lỗi nghiệp vụ trong service/socket layer dùng custom exceptions như `ValidationException`, `AuthenticationException`, `AuthorizationException`, `InvalidBidException`, `AuctionClosedException`, `InsufficientFundsException`, `ResourceNotFoundException`, `BusinessRuleException`.
- **JSON contract:** `RequestRouter` catch `BusinessException` và trả về `Response.fail(type, requestId, e.getMessage())`. Giao thức JSON không đổi: client vẫn nhận `success`, `message`, `data`; không có `errorCode`.
- **Fallback legacy:** `RequestRouter` vẫn catch `IllegalArgumentException | IllegalStateException` để tránh regression với guard clause/model/DAO hoặc code cũ chưa refactor.

## 6. Hướng dẫn Mở rộng

- **Thêm tính năng mới:**
    1. Định nghĩa `MessageType` mới trong `common`.
    2. Tạo DTO cần thiết trong `common/dto`.
    3. Triển khai xử lý tại `RequestRouter` và `Service` phía Server.
    4. Tạo View FXML và Controller tương ứng phía Client.
    5. Đăng ký điều hướng trong `SceneManager`.
