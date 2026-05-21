# Quy trình làm việc với Git

## Nhánh chính

- `main`: nhánh ổn định để demo/nộp bài.
- `dev`: nhánh tích hợp trước khi đưa lên `main`.
- `feature/<tên-người-làm>/<tên-task>`: nhánh cho từng thay đổi.

Ví dụ:

```bash
git switch dev
git pull origin dev
git switch -c feature/huy/readme-docs-ci
```

## Quy trình đề xuất

1. Tạo branch từ `dev`.
2. Thực hiện thay đổi có phạm vi rõ ràng.
3. Chạy kiểm thử:

   ```bash
   mvn clean verify
   ```

4. Chạy thử server/client nếu thay đổi ảnh hưởng hành vi runtime.
5. Commit theo Conventional Commits.
6. Push branch và tạo Pull Request vào `dev`.
7. Chỉ merge sau khi Huy review/approve.

## Commit message

Ví dụ:

```bash
git commit -m "fix: run JavaFX tests under virtual display in CI"
git commit -m "docs: refresh project documentation"
```

Loại commit thường dùng:

- `feat:` tính năng mới.
- `fix:` sửa lỗi.
- `docs:` cập nhật tài liệu.
- `test:` thêm hoặc sửa test.
- `refactor:` đổi cấu trúc code không đổi hành vi.
- `chore:` cấu hình, build, cleanup.

## Lưu ý khi merge

Không commit trực tiếp vào `main` hoặc `dev` nếu thay đổi chưa được kiểm thử. Sau khi pull code mới từ `dev`, nên chạy lại:

```bash
mvn clean install
```
