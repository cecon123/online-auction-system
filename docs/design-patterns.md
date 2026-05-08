# Design Patterns - Online Auction System

This document explains the design patterns used in the Online Auction System.

The project currently focuses on three required patterns:

```text
Singleton
Factory Method
Observer
```

Optional patterns can be added later if the core system is stable:

```text
Strategy
Command
```

---

## 1. Singleton

### Current implementations

```text
Database.getInstance()
JsonMapper.getInstance()
NotificationService.getInstance()
SessionManager.getInstance()
```

### Locations

```text
server/src/main/java/com/auction/server/dao/Database.java
server/src/main/java/com/auction/server/util/JsonMapper.java
server/src/main/java/com/auction/server/service/NotificationService.java
server/src/main/java/com/auction/server/service/SessionManager.java
```

### Purpose

- **Database**: Centralized SQLite connection manager.
- **JsonMapper**: Centralized Gson instance with custom type adapters.
- **NotificationService**: Manages realtime client subscriptions and broadcasting.
- **SessionManager**: Handles user sessions and token validation.

### Why Singleton is appropriate here

- Configuration consistency (SQLite and Gson).
- Resource efficiency (reuse one Gson instance, one database manager).
- Shared utility access across different server modules.

### Related files

```text
server/src/main/java/com/auction/server/dao/Database.java
server/src/main/java/com/auction/server/util/JsonMapper.java
server/src/main/java/com/auction/server/service/NotificationService.java
server/src/main/java/com/auction/server/service/SessionManager.java
```

---

## 2. Factory Method

### Current implementation

```text
ItemFactory.create(...)
```

### Location

```text
server/src/main/java/com/auction/server/factory/ItemFactory.java
```

### Purpose

The system has an abstract `Item` class and concrete item types:

```text
Item
├── Electronics
├── Art
└── Vehicle
```

The factory creates the correct concrete object based on `ItemType`.

### Why Factory Method is appropriate here

Without a factory, object creation logic would be duplicated in controllers, services, and tests.

Benefits:

- `ItemService` does not need to know all subclass constructors.
- Adding a new item type later is easier.
- The design demonstrates abstraction and polymorphism.
- Business services stay cleaner.

---

## 3. Observer

### Current implementation

Observer is implemented in the realtime bidding feature via `NotificationService`.

### Purpose

When a valid bid is placed, all clients watching the same auction receive an update immediately.

Flow:

```text
BidService.placeBid()
→ notificationService.broadcast(auctionId, MessageType.BID_UPDATE, updateData)
→ JavaFX Client receives BID_UPDATE
→ Platform.runLater(...) updates UI
```

### Why Observer is appropriate here

- Multiple clients can watch the same auction.
- The server does not need to know làm thế nào mỗi client hiển thị bản cập nhật.
- Clients có thể subscribe hoặc unsubscribe từ các sự kiện đấu giá.
- Cập nhật thời gian thực mà không cần polling.

### Event types

```text
BID_UPDATE
AUCTION_CLOSED
TIME_EXTENDED
```

### Subscription flow

```text
Client opens LiveBiddingView → sends SUBSCRIBE_AUCTION
Server registers client writer in NotificationService
```

### Client-side rule

JavaFX UI must not be updated directly from the socket listener thread. Always use `Platform.runLater()`.

---

## 4. Strategy - Optional

### Planned usage

Strategy can be used for bidding logic if the team implements Auto-Bidding.

Expected structure:

```text
BidStrategy
├── ManualBidStrategy
└── AutoBidStrategy
```

---

## 5. Command - Optional

### Possible usage

Command can be used to encapsulate bid actions.

---

## 6. Pattern Summary

| Pattern | Status | Main class | Purpose |
|---|---|---|---|
| Singleton | Implemented | `Database` | Centralized database connection configuration |
| Factory Method | Implemented | `ItemFactory` | Create concrete `Item` subclasses |
| Observer | Implemented | `NotificationService` | Realtime bid update |
| Strategy | Optional | `BidStrategy` | Manual bid vs auto bid |
| Command | Optional | `BidCommand` | Encapsulate bid action |

---

## 7. Explanation for Presentation

### Singleton explanation

`Database` và `NotificationService` là Singleton vì chúng quản lý tài nguyên dùng chung (DB connection và client subscriptions). Việc này giúp cấu hình nhất quán và tránh việc tạo nhiều instance gây lãng phí tài nguyên.

### Factory Method explanation

`ItemFactory` được dùng vì `Item` là lớp trừu tượng. Thay vì viết logic khởi tạo ở nhiều nơi, Factory giúp tạo đúng subclass dựa trên `ItemType`, giúp code sạch và dễ mở rộng.

### Observer explanation

Observer được triển khai qua `NotificationService`. Khi có bid mới, server sẽ "đẩy" thông báo tới tất cả client đang theo dõi phiên đấu giá đó, đảm bảo tính realtime mà không cần client phải liên tục hỏi server (polling).

---

## 8. Notes for Team Members

- Sử dụng `ItemFactory` khi tạo đối tượng Item.
- Mọi cập nhật UI JavaFX từ sự kiện socket phải nằm trong `Platform.runLater()`.
- Logic thông báo realtime nên nằm trong `NotificationService`.
