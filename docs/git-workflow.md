# Quy trình làm việc với Git (Git Workflow)

Tài liệu này phác thảo chiến lược phân nhánh và cộng tác cho dự án Hệ thống Đấu giá Trực tuyến.

## 1. Chiến lược Phân nhánh (Branching Strategy)

- **`main`**: Nhánh ổn định. Chứa mã nguồn mới nhất đã sẵn sàng để demo. Không cho phép commit trực tiếp vào nhánh này.
- **`dev`**: Nhánh tích hợp. Tất cả các nhánh tính năng đều được merge vào đây trước.
- **`feature/<tên-tính-năng>-<tên-người-làm>`**: Nhánh làm việc cá nhân cho từng tác vụ. 

Ví dụ: `feature/sqlite-user-dao-manh`

## 2. Quy trình làm việc hàng ngày

### Bước 1: Đồng bộ nhánh `dev` địa phương
Trước khi bắt đầu một tác vụ mới, hãy đảm bảo nhánh tích hợp của bạn đã được cập nhật.

```bash
git checkout dev
git pull origin dev
```

### Bước 2: Tạo nhánh tính năng mới
```bash
git checkout -b feature/my-new-task-huy
```

### Bước 3: Làm việc và Commit
Tuân thủ chuẩn [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` cho các tính năng mới.
- `fix:` cho các bản sửa lỗi.
- `docs:` cho cập nhật tài liệu.
- `refactor:` cho việc cấu trúc lại mã nguồn.
- `test:` cho việc thêm/cập nhật kiểm thử.

```bash
git add .
git commit -m "feat: add auction lock manager"
```

### Bước 4: Merge vào nhánh `dev`
Sau khi tác vụ hoàn thành và đã được kiểm thử tại máy địa phương:

1. Đẩy nhánh của bạn lên:
   ```bash
   git push -u origin feature/my-new-task-huy
   ```
2. Kéo nhánh `dev` mới nhất vào nhánh tính năng để giải quyết xung đột:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout feature/my-new-task-huy
   git merge dev
   ```
3. Chạy kiểm thử lần cuối:
   ```bash
   mvn clean test
   ```
4. Merge vào `dev`:
   ```bash
   git checkout dev
   git merge feature/my-new-task-huy
   git push origin dev
   ```

## 3. Pull Requests & Kiểm duyệt mã nguồn (Code Review)

- Đối với các thay đổi lớn, hãy tạo Pull Request (PR) trên GitHub từ `feature/*` sang `dev`.
- Gắn thẻ (tag) Nhóm trưởng (Huy) để kiểm duyệt.
- Sau khi được phê duyệt, Nhóm trưởng sẽ thực hiện merge nhánh.

## 4. Quy tắc sau khi Merge

Mỗi khi bạn kéo (pull) mã nguồn mới từ `dev`, **bắt buộc** phải chạy lệnh:

```bash
mvn clean install
```

Việc này đảm bảo các thư mục `target` địa phương và các phụ thuộc (dependencies) được đồng bộ trên toàn bộ dự án multi-module.
