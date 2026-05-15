# Hướng dẫn Cho Thành viên mới (Developer Onboarding)

Chào mừng bạn gia nhập đội ngũ phát triển AuctionPro! Tài liệu này giúp bạn nhanh chóng nắm bắt quy trình và bắt đầu đóng góp cho dự án.

## 1. Quy trình Làm việc (Workflow)

Chúng tôi sử dụng **Enterprise Git Workflow** để quản lý mã nguồn:

1. **Nhánh Chính:**
    - `main`: Chứa mã nguồn ổn định nhất, sẵn sàng triển khai.
    - `dev`: Nhánh tích hợp cho các tính năng đang phát triển.
2. **Nhánh Tính năng:**
    - Tạo nhánh mới từ `dev` theo cú pháp: `feature/<tên-người-làm>/<tên-task>`.
    - Ví dụ: `feature/huy/bid-auto-refresh`.
3. **Pull Request (PR):**
    - Sau khi hoàn thành và tự kiểm thử, tạo PR vào nhánh `dev`.
    - Phải có ít nhất một thành viên (Reviewer) approve trước khi merge.

## 2. Quy ước Lập trình (Coding Conventions)

- **Ngôn ngữ:**
    - Code (Biến, Hàm, Lớp, Comment): **Tiếng Anh**.
    - Tài liệu hướng dẫn & Báo cáo: **Tiếng Việt**.
- **Tiêu chuẩn Code:**
    - Tuân thủ Google Java Style Guide.
    - Sử dụng `final` cho các biến không thay đổi.
    - Ưu tiên Composition (Thành phần) hơn Inheritance (Kế thừa).
- **Logging:**
    - Tuyệt đối không sử dụng `System.out.println`.
    - Sử dụng SLF4J: `logger.info()`, `logger.error()`, `logger.debug()`.

## 3. Quy trình Commit Nghiêm ngặt

Trước khi thực hiện `git commit`, bạn BẮT BUỘC phải:
1. Chạy `mvn clean install` để đảm bảo không lỗi biên dịch.
2. Chạy `mvn test` để đảm bảo không lỗi logic (Regressions).
3. Chạy thực tế (Server + Client) để kiểm tra tính ổn định.
4. Commit message theo chuẩn **Conventional Commits**:
    - `feat: ...` (Tính năng mới)
    - `fix: ...` (Sửa lỗi)
    - `docs: ...` (Cập nhật tài liệu)
    - `chore: ...` (Cấu hình, build...)

## 4. Các bước cho ngày đầu tiên

1. Clone dự án và cài đặt môi trường (Theo [Setup Guide](setup.md)).
2. Đọc hiểu giao thức Socket tại [API Protocol](../protocol.md).
3. Chạy thử hệ thống với tài khoản admin mẫu.
4. Nhận task đầu tiên từ bảng điều khiển (Task Board) trong README.
5. Tạo nhánh và bắt đầu hành trình phát triển của bạn!
