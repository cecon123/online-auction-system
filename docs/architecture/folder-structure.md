# Cấu trúc Thư mục (Folder Structure)

Dự án AuctionPro được tổ chức theo mô hình Maven Multi-module để tách biệt trách nhiệm và tái sử dụng mã nguồn.

## 1. Cấu trúc Tổng thể
```text
C:\online-auction-system\
├── common/             # Mã nguồn dùng chung (DTO, Model, Protocol)
├── server/             # Module Backend (Logic, Database, Socket Server)
├── client/             # Module Frontend (JavaFX UI, Socket Client)
├── docs/               # Tài liệu dự án (Architecture, Manuals, diagrams)
└── uploads/            # Thư mục lưu trữ tài sản tĩnh (Ảnh mặt hàng)
```

## 2. Chi tiết từng Module

### 2.1 Module `common` (Shared)
Chứa các thành phần mà cả Client và Server đều cần sử dụng để hiểu nhau.
- `dto/`: Các đối tượng vận chuyển dữ liệu (Data Transfer Objects) để đóng gói dữ liệu JSON.
- `enums/`: Các kiểu dữ liệu liệt kê (Role, AuctionStatus, ItemType).
- `model/`: Các thực thể domain (User, Auction, Item).
- `protocol/`: Định nghĩa `Request`, `Response` và các loại thông điệp `MessageType`.

### 2.2 Module `server` (Backend)
- `dao/`: Lớp truy cập dữ liệu (Data Access Object) thực hiện các câu lệnh SQL tới SQLite.
- `service/`: Chứa toàn bộ logic nghiệp vụ (Xử lý đặt thầu, kết thúc đấu giá, ví tiền).
- `socket/`: Quản lý kết nối TCP. `RequestRouter` làm lớp điều phối mỏng và chuyển tiếp tới các handler theo nhóm nghiệp vụ (`AuthRequestHandler`, `AuctionRequestHandler`, `BidRequestHandler`, `WalletRequestHandler`, `AdminRequestHandler`, `SubscriptionRequestHandler`).
- `concurrency/`: Xử lý khóa (Locking) và an toàn đa luồng cho các phiên đấu giá cao điểm.

### 2.3 Module `client` (Frontend)
- `controller/`: Các lớp Java điều khiển hành vi của giao diện (FXML).
- `service/`: Các lớp proxy gửi yêu cầu tới server và nhận kết quả bất đồng bộ.
- `socket/`: `SocketClient` quản lý kết nối lâu dài và lắng nghe sự kiện từ server.
- `util/`: Các tiện ích như `SceneManager` (điều hướng màn hình), `NotificationManager`, và `AuctionStatusUi` để chuẩn hóa badge/status CSS.
- `resources/`: 
    - `fxml/`: Định nghĩa giao diện người dùng bằng XML.
    - `css/`: Các file stylesheet hiện đại cho JavaFX.
