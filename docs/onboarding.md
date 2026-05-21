# Hướng dẫn cho thành viên mới

Tài liệu này giúp thành viên trong nhóm nắm nhanh cách build, chạy và đóng góp cho AuctionPro.

## Quy trình branch

- `main`: mã ổn định để demo/nộp bài.
- `dev`: nhánh tích hợp.
- `feature/<tên-người-làm>/<tên-task>`: nhánh làm việc cho từng task.

Ví dụ:

```bash
git switch dev
git pull origin dev
git switch -c feature/huy/auth-service
```

## Quy ước code

- Tên class, method, variable, comment, log và exception message dùng tiếng Anh.
- Tài liệu và trao đổi nhóm có thể dùng tiếng Việt.
- Không dùng `System.out.println` trong code logic; dùng SLF4J + Logback.
- Commit message theo Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`.

## Kiểm tra trước khi tạo Pull Request

```bash
mvn clean verify
```

Sau đó chạy thủ công:

```bash
mvn -pl server exec:java
mvn -pl client javafx:run
```

Kiểm tra ít nhất luồng đăng nhập, tạo phiên đấu giá, nạp tiền, đặt giá và cập nhật realtime giữa hai client.

## Tài liệu cần đọc

1. [Setup guide](setup.md)
2. [Socket protocol](protocol.md)
3. [Class diagram](class-diagram.md)
4. [Testing guide](testing.md)
5. [Git workflow](git-workflow.md)
