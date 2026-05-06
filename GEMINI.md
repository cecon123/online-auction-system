# Project Instructions: Online Auction System (AuctionPro)

Chào mừng các thành viên đội phát triển. Đây là hướng dẫn phối hợp công việc thông qua Gemini CLI.

## 1. Ngôn ngữ & Giao tiếp
- **Ngôn ngữ làm việc:** Sử dụng **Tiếng Việt** làm ngôn ngữ chính trong toàn bộ quá trình trao đổi, giải thích code, viết báo cáo tiến độ và comment trong code (trừ các từ chuyên ngành kỹ thuật).
- **Phản hồi của AI:** Mọi phản hồi từ Gemini CLI phải sử dụng Tiếng Việt chuyên nghiệp, súc tích và tập trung vào kỹ thuật.

## 2. Vai trò & Phân công
- **Huy (Lead):** Kiến trúc tổng thể, Protocol, Socket Server, Concurrency, Realtime, Security, Reviewer chính.
- **Mạnh:** SQLite DAO/Repository, Unit Test Backend, CI/CD.
- **Linh:** Login/Register UI, Dashboard, AppShell, Socket Client.
- **Hải Anh:** Live Bidding UI, Seller Screens, Realtime Chart.

## 3. Quy trình làm việc & Git Workflow chuẩn
Quy trình này áp dụng cho mọi thành viên để đảm bảo code luôn sạch và chạy được:

1.  **Sync:** Luôn pull code mới nhất từ `dev` trước khi bắt đầu: `git pull origin dev`.
2.  **Branching:** Tạo branch theo cú pháp: `feature/<tên-người-làm>/<tên-task>`.
    *   Ví dụ: `feature/manh/sqlite-item-dao`, `feature/linh/socket-client-foundation`.
3.  **Local Develop & Test:** Code và chạy thử theo đúng mục 6.
4.  **Commit & Push:** Commit theo chuẩn Conventional Commits và push lên remote.
5.  **Pull Request (PR):** Tạo PR từ branch feature vào branch `dev` trên GitHub.
6.  **Code Review:** Thông báo cho **Huy (Lead)** review code trên PR.
    *   Nếu có yêu cầu sửa đổi (Request changes): Tiếp tục sửa và push thêm commit vào cùng branch đó.
    *   Nếu được phê duyệt (Approve): Huy (hoặc AI dưới quyền Huy) sẽ thực hiện **Merge** vào `dev`.
7.  **Clean up:** Sau khi merge, xóa branch cục bộ để tránh nhầm lẫn.

## 4. Kiến trúc & Công nghệ
- **Stack:** Java 21, Maven, JavaFX, SQLite.
- **Mô hình:** Client-Server qua TCP Socket.
- **Protocol:** Newline-delimited JSON. (Tuyệt đối không sử dụng Pretty Print khi gửi qua socket).
- **Cấu trúc Server:** Controller -> Service -> DAO.
- **Cấu trúc Client:** FXML (View) -> Controller -> ClientService.

## 5. Chỉ dẫn cho Gemini CLI
- **Context:** Luôn đọc `docs/protocol.md` và `docs/class-diagram.md` trước khi sửa đổi.
- **Surgical Update:** Sử dụng công cụ `replace` một cách chính xác, tránh ghi đè toàn bộ file lớn.
- **Testing:** Mỗi khi sửa logic backend (DAO/Service), PHẢI cập nhật hoặc tạo mới Unit Test tương ứng.
- **Validation:** Chạy `mvn test` trước khi hoàn thành task.

## 6. Quy trình Kiểm thử & Commit bắt buộc (Mandatory Verification)
AI Agent PHẢI tuân thủ quy trình nghiêm ngặt sau đây trước khi thực hiện `commit`:
1.  **Xác nhận chạy thử:** Sau khi hoàn thành code, AI PHẢI yêu cầu người dùng chạy các lệnh sau:
    *   `mvn clean install` (Build hệ thống).
    *   `mvn -pl server exec:java` (Chạy Server).
    *   `mvn -pl client javafx:run` (Chạy Client).
2.  **Kiểm tra tính ổn định:** 
    *   Nếu người dùng báo có lỗi hoặc hệ thống không ổn định, AI PHẢI tiếp tục sửa lỗi cho đến khi ổn định.
    *   **TUYỆT ĐỐI KHÔNG** commit khi code vẫn còn lỗi biên dịch hoặc lỗi Runtime.
3.  **Chỉ commit khi ổn định:** Chỉ khi người dùng xác nhận "Đã chạy thử ổn định", AI mới được tiến hành `git add` và `git commit`.

## 7. Quy ước Code (Coding Standards)
- **Naming:** CamelCase cho class, camelCase cho method/variable.
- **OOP:** Ưu tiên Composition hơn Inheritance trừ các Model cốt lõi đã định nghĩa.
- **Patterns:** Sử dụng Singleton cho Database Manager, Factory cho Item creation, Observer cho Realtime update.

## 8. Cập nhật Tiến độ (Progress Tracking)
Sau mỗi task được merge vào `dev`:
1.  Cập nhật trạng thái [x] vào mục **"16. Task Board"** trong `README.md`.
2.  Mô tả ngắn gọn thay đổi trong nội dung commit.

## 9. Hướng dẫn Kiểm thử thủ công (Manual Testing Guide)
Để hỗ trợ việc kiểm tra logic Backend khi UI chưa hoàn thiện hoặc để debug giao thức Socket:

### 9.1 Chạy hệ thống
- **Khởi động Server:** Mở terminal, chạy `mvn -pl server exec:java`. (Mặc định chạy tại port 8080).
- **Khởi động Client:** Mở terminal khác, chạy `mvn -pl client javafx:run`.

### 9.2 Test Socket bằng Ncat (Bắt buộc cho Backend)
Khi UI chưa hoàn thiện hoặc cần test logic JSON "sạch", sử dụng `ncat` (hoặc telnet):
1. Kết nối: `ncat localhost 8080`.
2. Gửi lệnh mẫu (Copy nguyên dòng và dán vào Ncat):
   - **Đăng ký:** `{"type":"REGISTER","requestId":"req-001","token":null,"data":{"fullName":"Test User","username":"test01","password":"password123","role":"BIDDER"}}`
   - **Đăng nhập:** `{"type":"LOGIN","requestId":"req-002","token":null,"data":{"username":"test01","password":"password123"}}`

### 9.3 Quan sát Log để Debug
- Luôn quan sát Terminal chạy **Server**. Nếu logic đúng, sẽ thấy các dòng Log bắt đầu bằng `[Request]` và `[AuthService]`.
- Nếu không thấy Log, cần kiểm tra lại kết nối Socket hoặc logic điều hướng trong `RequestRouter`.
