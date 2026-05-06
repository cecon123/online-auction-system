# Project Instructions: Online Auction System (AuctionPro)

Chào mừng các thành viên đội phát triển. Đây là hướng dẫn phối hợp công việc thông qua Gemini CLI.

## 1. Ngôn ngữ & Giao tiếp
- **Ngôn ngữ làm việc:** Sử dụng **Tiếng Việt** làm ngôn ngữ chính trong toàn bộ quá trình trao đổi, giải thích code, viết báo cáo tiến độ và comment trong code (trừ các từ chuyên ngành kỹ thuật).
- **Phản hồi của AI:** Mọi phản hồi từ Gemini CLI phải sử dụng Tiếng Việt chuyên nghiệp, súc tích và tập trung vào kỹ thuật.

## 2. Vai trò & Phân công
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
## 6. Quy trình Kiểm thử bắt buộc (Mandatory Verification)
Trước khi `commit` và `push`, mỗi thành viên (và Agent của họ) PHẢI thực hiện các bước sau:
1.  **Build toàn bộ:** `mvn clean install` (Để đảm bảo không làm gãy dependency của các module khác).
2.  **Chạy Server:** `mvn -pl server exec:java` (Kiểm tra xem server có khởi động lỗi không).
3.  **Chạy Client:** `mvn -pl client javafx:run` (Kiểm tra giao diện và kết nối mock/thật).
4.  **Chạy Test:** `mvn test` (Đảm bảo không làm gãy các logic cũ).

> **Lưu ý:** Agent chỉ được coi là hoàn thành task khi đã báo cáo kết quả chạy các lệnh trên cho người dùng.

## 7. Cập nhật Tiến độ (Progress Tracking)
Để Lead (Huy) nắm bắt được tình hình, sau mỗi task hoàn thành:
1.  Agent phải cập nhật trạng thái [x] vào mục tương ứng trong `README.md`.
2.  Nếu là tính năng mới chưa có trong danh sách, Agent phải thêm một dòng vào phần **"16. Immediate action plan"** hoặc **"21. Trạng thái demo hiện tại"** trong `README.md`.
3.  Mô tả ngắn gọn thay đổi trong nội dung commit (theo Conventional Commits).
