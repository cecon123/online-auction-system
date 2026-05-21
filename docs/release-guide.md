# Hướng dẫn tạo GitHub Release cho phiên bản mới

Workflow GitHub Actions tự động build, test và tạo release khi repository có tag version dạng `v*`, ví dụ `v1.0.0`.

## Khi nào CI/CD chạy

- Push vào `dev` hoặc `main`: chạy `mvn clean verify` trên Ubuntu và upload JAR artifact của lần build CI.
- Pull Request vào `dev` hoặc `main`: chạy `mvn clean verify`.
- Push tag dạng `v1.0.0`: chạy verify, build release artifacts trên Linux/Windows/macOS và tạo GitHub Release.
- Chạy thủ công bằng **Actions** > **Build, Test, and Release** > **Run workflow**: nhập tag đã tồn tại, ví dụ `v1.0.0`, để build lại artifact và tạo/cập nhật release cho tag đó.

## Artifact trong release

Mỗi release sẽ có:

- `auction-server-<version>.jar`: server fat JAR.
- `auction-client-linux-<version>.jar`: client fat JAR build trên Linux.
- `auction-client-windows-<version>.jar`: client fat JAR build trên Windows.
- `auction-client-macos-<version>.jar`: client fat JAR build trên macOS.
- `auctionpro-report-<version>.pdf`: báo cáo PDF nếu file `docs/pdf/auctionpro-report.pdf` tồn tại.

Client JavaFX dùng native dependency theo hệ điều hành, nên release tách client JAR theo platform. Khi demo trên Windows, dùng file `auction-client-windows-<version>.jar`.

## Quy trình cho version tiếp theo

1. Đảm bảo code cuối đã được merge vào nhánh nộp/release, thường là `main`.

   ```bash
   git switch main
   git pull origin main
   ```

2. Cập nhật version nếu cần trong `pom.xml`, README hoặc tài liệu release.

3. Chạy kiểm tra local:

   ```bash
   mvn clean verify
   mvn clean package
   java -jar server/target/auction-server.jar
   java -jar client/target/auction-client.jar
   ```

4. Commit các thay đổi version/tài liệu nếu có:

   ```bash
   git add .
   git commit -m "chore: prepare release v1.0.1"
   git push origin main
   ```

5. Tạo tag version và push tag:

   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1"
   git push origin v1.0.1
   ```

6. Mở tab **Actions** trên GitHub để theo dõi workflow `Build, Test, and Release`.

7. Sau khi workflow pass, kiểm tra tab **Releases**. Release mới sẽ có các JAR và PDF đính kèm.

## Nếu cần sửa release

Nếu tag đã push nhưng release lỗi do bước publish, không cần tạo lại tag ngay. Mở **Actions** > **Build, Test, and Release** > **Run workflow**, nhập tag cần release, ví dụ `v1.0.0`, rồi chạy lại. Workflow sẽ checkout đúng tag, build lại JAR và tạo release; nếu release đã tồn tại thì upload lại asset bằng chế độ ghi đè.

Nếu lỗi nằm ở source code hoặc artifact của tag đã sai nội dung, ưu tiên sửa trên branch mới, merge vào `dev`, merge tiếp vào `main`, rồi tạo version mới như `v1.0.1` hoặc `v1.0.2`. Chỉ xóa/recreate tag khi chắc chắn tag đó chưa được dùng để nộp hoặc chia sẻ.

Nếu release job fail ở bước **Create GitHub Release**, kiểm tra thêm mục **Settings** > **Actions** > **General** > **Workflow permissions**. Repository cần cho phép GitHub Actions tạo release bằng quyền ghi nội dung repository.
