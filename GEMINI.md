# Project Instructions: Online Auction System (AuctionPro)

Chào mừng các thành viên đội phát triển. Đây là hướng dẫn phối hợp công việc thông qua Gemini CLI.

## 1. Ngôn ngữ & Giao tiếp
- **Giao tiếp với AI:** Sử dụng **Tiếng Việt** chuyên nghiệp, súc tích trong toàn bộ quá trình trao đổi, giải thích yêu cầu và báo cáo tiến độ.
- **Ngôn ngữ trong mã nguồn:** BẮT BUỘC sử dụng **Tiếng Anh** cho:
    - Tên class, method, variable.
    - Toàn bộ Comment trong code.
    - Nội dung Log (SLF4J).
    - Các thông báo lỗi (Exception messages) và Message phản hồi trong JSON protocol.

## 2. Vai trò & Phân công
- **Huy (Lead):** Kiến trúc tổng thể, Security, Concurrency, Reviewer chính (Người duy nhất có quyền Approve merge vào `dev`).
- **Mạnh:** SQLite DAO/Repository, Unit Test Backend, CI/CD.
- **Linh:** Login/Register UI, Dashboard, Socket Client Foundation.
- **Hải Anh:** Live Bidding UI, Seller Screens, Realtime Chart, UI Integration.

## 3. Quy trình làm việc (Enterprise Git Workflow)
1.  **Branching:** `feature/<tên-người-làm>/<tên-task>`. (Ví dụ: `feature/huy/auth-service`).
2.  **Review bắt buộc:** Sau khi hoàn thành code trên branch riêng, AI PHẢI nhắc nhở thành viên tạo Pull Request và thông báo cho **Huy (Lead)** review.
3.  **Merge:** CHỈ khi Huy đã **Approve** trên GitHub, AI mới được hỗ trợ thực hiện lệnh merge branch đó vào `dev`.
4.  **Commit Message:** Tuân thủ [Conventional Commits](https://www.conventionalcommits.org/).

## 4. Quy trình Kiểm thử & Commit nghiêm ngặt
AI Agent PHẢI tuân thủ quy trình sau trước khi thực hiện `commit`:
1.  **Xác nhận chạy thử:** AI yêu cầu người dùng chạy build và khởi động thực tế:
    *   `mvn clean install`
    *   `mvn -pl server exec:java`
    *   `mvn -pl client javafx:run`
2.  **Tính ổn định:** 
    *   Nếu hệ thống còn lỗi (biên dịch hoặc runtime), AI PHẢI tiếp tục sửa lỗi và cập nhật cho đến khi ổn định.
    *   **KHÔNG ĐƯỢC commit** khi code chưa chạy thông suốt hoặc chưa Pass Unit Test.
3.  **Xác nhận từ người dùng:** Chỉ khi người dùng xác nhận "Hệ thống ổn định", AI mới tiến hành `git add` và `git commit`.

## 5. Tiêu chuẩn Kỹ thuật & Coding Standards
- **Logging:** Sử dụng **SLF4J + Logback**. Tuyệt đối không dùng `System.out.println` trong code logic.
- **Bảo mật:** Mật khẩu phải được băm bằng **BCrypt** trước khi lưu vào SQLite.
- **OOP:** Ưu tiên Composition hơn Inheritance. Tuân thủ đúng Class Diagram trong `docs/`.
- **Protocol:** Newline-delimited JSON (không pretty-print khi gửi qua socket).

## 6. Chỉ dẫn cho Gemini CLI
- **Context:** Luôn đọc `docs/protocol.md` và `docs/class-diagram.md` trước khi sửa đổi DTO hoặc logic quan trọng.
- **Surgical Update:** Ưu tiên sử dụng công cụ `replace` chính xác thay vì ghi đè toàn bộ file lớn.
- **Validation:** Luôn chạy `mvn test` để đảm bảo không có regressions.

## 7. Cập nhật Tiến độ (BẮT BUỘC)
AI Agent và thành viên PHẢI thực hiện:
1.  Cập nhật trạng thái `[x]` vào mục **"15. Task Board"** trong `README.md` ngay sau khi hoàn thành logic của mỗi Task và trước khi thực hiện `git commit`.
2.  Ghi tóm tắt thay đổi vào nội dung commit.
3.  Thông báo cho Lead sau khi đã Push và tạo PR.

## 8. Hướng dẫn Kiểm thử thủ công (Manual Testing Guide)
### 8.1 Chạy hệ thống
- **Server:** `mvn -pl server exec:java` (Port 8080).
- **Client:** `mvn -pl client javafx:run`.

### 8.2 Test Socket bằng Ncat (Backend Test)
Sử dụng `ncat localhost 8080` và gửi các dòng JSON mẫu:
- **Register:** `{"type":"REGISTER","requestId":"req-001","token":null,"data":{"fullName":"Test User","username":"test01","password":"password123","role":"BIDDER"}}`
- **Login:** `{"type":"LOGIN","requestId":"req-002","token":null,"data":{"username":"test01","password":"password123"}}`

### 8.3 Quan sát Log
Theo dõi cửa sổ Server để thấy các log SLF4J (INFO/ERROR) nhằm debug luồng xử lý.

## 9. Agent Skills (Kỹ năng hỗ trợ)
Dự án khuyến khích sử dụng các Agent Skill sau để tối ưu hóa quy trình:
- `web-design-guidelines`: Sử dụng khi cần review giao diện JavaFX, kiểm tra tính thẩm mỹ và UX theo chuẩn.
- `java-refactoring-extract-method`: Hỗ trợ tái cấu trúc các phương thức Java phức tạp, giúp mã nguồn sạch và dễ bảo trì hơn.
- `skill-creator`: Sử dụng khi muốn tạo thêm các skill tùy chỉnh mới cho dự án.
- `find-skills`: Tìm kiếm các skill bổ trợ khác có sẵn trong hệ thống.
