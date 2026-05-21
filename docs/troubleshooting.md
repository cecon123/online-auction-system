# Khắc phục sự cố

## Client báo không kết nối được server

Nguyên nhân thường gặp:

- Server chưa chạy.
- Server không chạy ở port `8080`.
- Port bị firewall hoặc ứng dụng khác chặn.

Cách kiểm tra:

```bash
mvn -pl server exec:java
```

Nếu đổi port server bằng `-Dserver.port=...`, client hiện cần được chỉnh cấu hình/field tương ứng trong code trước khi chạy.

## JavaFX không khởi động được

Khi chạy client thật, máy cần môi trường đồ họa. Lỗi thường gặp trên Linux headless hoặc WSL chưa cấu hình display:

```text
Unable to open DISPLAY
```

Cách xử lý:

- Chạy client trên Windows, macOS hoặc Linux desktop.
- Với CI/test headless, chạy Maven dưới display ảo:

  ```bash
  xvfb-run -a mvn clean verify
  ```

## Build lỗi sau khi pull code mới

Với Maven multi-module, hãy build từ thư mục gốc để module `common` được biên dịch trước:

```bash
mvn clean install
```

Nếu chỉ chạy một module riêng lẻ và gặp lỗi thiếu dependency nội bộ, chạy lại lệnh trên ở root project.

## Database bị khóa

Lỗi `database is locked` thường xuất hiện khi nhiều tiến trình cùng ghi vào một file SQLite hoặc có transaction chưa đóng.

Cách xử lý:

- Đảm bảo chỉ có một server đang chạy trên cùng database.
- Tắt tool SQLite đang mở file `auction.db` nếu tool đó giữ lock ghi.
- Kiểm tra log server để tìm transaction hoặc request lỗi trước đó.

## Muốn reset dữ liệu demo

Tắt server rồi xóa file database đang dùng, mặc định là:

```text
auction.db
```

Khi chạy lại server, schema và seed data sẽ được tạo lại nếu không truyền `-Dauction.skip.seed=true`.

## Ảnh mặt hàng không hiển thị

Kiểm tra:

- File ảnh có tồn tại trong thư mục `uploads`.
- Asset server đang chạy ở port `8081`.
- URL ảnh trong log/server response có đúng host và port không.
