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

## 3. Quy trình làm việc (Git Workflow)
- **Branch chính:** `main` (demo), `dev` (tích hợp).
- **Quy tắc Branching:** Mọi tính năng PHẢI làm trên branch riêng: `feature/<tên-task>-<tên-người-làm>`.
- **Review bắt buộc:** Sau khi hoàn thành code trên branch riêng, AI Agent PHẢI nhắc nhở thành viên thông báo cho **Huy (Lead)** để review code. **CHỈ khi được Huy phê duyệt (Approve)**, AI mới được phép hỗ trợ merge branch đó vào `dev`.
- **Merge:** Sau khi merge vào `dev`, chạy `mvn clean install` để đồng bộ.
- **Commit Message:** Tuân thủ [Conventional Commits](https://www.conventionalcommits.org/).

## 4. Kiến trúc & Công nghệ
- **Stack:** Java 21, Maven, JavaFX, SQLite.
- **Mô hình:** Client-Server qua TCP Socket.
- **Protocol:** Newline-delimited JSON. (Tuyệt đối không sử dụng Pretty Print khi gửi qua socket).
- **Cấu trúc Server:** Controller -> Service -> DAO.
- **Cấu trúc Client:** FXML (View) -> Controller -> ClientService.

## 5. Chỉ dẫn cho Gemini CLI
- **Context:** Luôn đọc `docs/protocol.md` và `docs/class-diagram.md` trước khi sửa đổi DTO hoặc logic quan trọng.
- **Surgical Update:** Sử dụng công cụ `replace` một cách chính xác, tránh ghi đè toàn bộ file lớn.
- **Testing:** Mỗi khi sửa logic backend (DAO/Service), PHẢI cập nhật hoặc tạo mới Unit Test tương ứng trong `src/test/java`.
- **Validation:** Chạy `mvn test` trước khi kết thúc bất kỳ task nào.

## 6. Quy trình Kiểm thử & Commit bắt buộc (Mandatory Verification)
AI Agent PHẢI tuân thủ quy trình nghiêm ngặt sau đây trước khi thực hiện `commit`:
1.  **Xác nhận chạy thử:** Sau khi hoàn thành code, AI PHẢI yêu cầu người dùng chạy các lệnh sau:
    *   `mvn clean install` (Build hệ thống).
    *   `mvn -pl server exec:java` (Chạy Server).
    *   `mvn -pl client javafx:run` (Chạy Client).
2.  **Kiểm tra tính ổn định:** 
    *   Nếu người dùng báo có lỗi hoặc hệ thống không ổn định, AI PHẢI tiếp tục phân tích, sửa lỗi và cập nhật cho đến khi ổn định.
    *   **TUYỆT ĐỐI KHÔNG** commit khi code vẫn còn lỗi biên dịch hoặc lỗi Runtime khi chạy thử.
3.  **Chỉ commit khi ổn định:** Chỉ khi người dùng xác nhận "Đã chạy thử ổn định", AI mới được tiến hành `git add` và `git commit`.
4.  **Chạy Test:** Đảm bảo `mvn test` pass trước khi push.

## 7. Quy ước Code (Coding Standards)
- **Naming:** CamelCase cho class, camelCase cho method/variable.
- **OOP:** Ưu tiên Composition hơn Inheritance trừ các Model cốt lõi đã định nghĩa.
- **Patterns:** Sử dụng Singleton cho Database Manager, Factory cho Item creation, Observer cho Realtime update.

## 8. Cập nhật Tiến độ (Progress Tracking)
Để Lead (Huy) nắm bắt được tình hình, sau mỗi task hoàn thành:
1.  Agent phải cập nhật trạng thái [x] vào mục tương ứng trong `README.md`.
2.  Nếu là tính năng mới chưa có trong danh sách, Agent phải thêm một dòng vào phần **"16. Immediate action plan"** hoặc **"21. Trạng thái demo hiện tại"** trong `README.md`.
3.  Mô tả ngắn gọn thay đổi trong nội dung commit (theo Conventional Commits).
