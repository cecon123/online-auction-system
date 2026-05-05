# Project Instructions: Online Auction System (AuctionPro)

Chào mừng các thành viên đội phát triển. Đây là hướng dẫn phối hợp công việc thông qua Gemini CLI.

## 1. Vai trò & Phân công
- **Huy (Lead):** Kiến trúc tổng thể, Protocol, Socket Server, Concurrency, Realtime.
- **Mạnh:** SQLite DAO/Repository, Unit Test Backend, CI/CD.
- **Linh:** Login/Register UI, Dashboard, AppShell, Socket Client.
- **Hải Anh:** Live Bidding UI, Seller Screens, Realtime Chart.

## 2. Quy trình làm việc (Git Workflow)
- **Branch chính:** `main` (demo), `dev` (tích hợp).
- **Quy tắc Branching:** Mọi tính năng PHẢI làm trên branch riêng: `feature/<tên-task>-<tên-người-làm>`.
- **Merge:** Sau khi hoàn thành, tạo PR/Review. Sau khi merge vào `dev`, chạy `mvn clean install` để đồng bộ.
- **Commit Message:** Tuân thủ [Conventional Commits](https://www.conventionalcommits.org/):
    - `feat:` tính năng mới.
    - `fix:` sửa lỗi.
    - `refactor:` cấu trúc lại code.
    - `test:` thêm/sửa test.

## 3. Kiến trúc & Công nghệ
- **Stack:** Java 21, Maven, JavaFX, SQLite.
- **Mô hình:** Client-Server qua TCP Socket.
- **Protocol:** Newline-delimited JSON. (Tuyệt đối không sử dụng Pretty Print khi gửi qua socket).
- **Cấu trúc Server:** Controller -> Service -> DAO.
- **Cấu trúc Client:** FXML (View) -> Controller -> ClientService.

## 4. Chỉ dẫn cho Gemini CLI
- **Context:** Luôn đọc `docs/protocol.md` và `docs/class-diagram.md` trước khi sửa đổi DTO hoặc logic quan trọng.
- **Surgical Update:** Sử dụng công cụ `replace` một cách chính xác, tránh ghi đè toàn bộ file lớn.
- **Testing:** Mỗi khi sửa logic backend (DAO/Service), PHẢI cập nhật hoặc tạo mới Unit Test tương ứng trong `src/test/java`.
- **Validation:** Chạy `mvn test` trước khi kết thúc bất kỳ task nào.

## 5. Quy ước Code (Coding Standards)
- **Naming:** CamelCase cho class, camelCase cho method/variable.
- **OOP:** Ưu tiên Composition hơn Inheritance trừ các Model cốt lõi đã định nghĩa.
- **Patterns:** Sử dụng Singleton cho Database Manager, Factory cho Item creation, Observer cho Realtime update.
- **Concurrency:** Sử dụng `ReentrantLock` trong `AuctionLockManager` để bảo vệ các thao tác đấu giá.
