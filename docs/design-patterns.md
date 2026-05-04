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

### Current implementation

```text
Database.getInstance()
```

### Location

```text
server/src/main/java/com/auction/server/dao/Database.java
```

### Purpose

The `Database` class is responsible for creating SQLite connections. It is implemented as a Singleton so the server has a centralized place for database connection configuration.

### Why Singleton is appropriate here

- SQLite configuration should be consistent.
- PRAGMA settings are applied in one place.
- DAOs do not need to create their own database manager.
- The client module never accesses this class directly.
- It helps enforce the rule that only the server accesses the database.

### Important note

`Database` is a Singleton manager, but it does **not** keep one global `Connection` open forever.

Instead, it creates a new connection when needed:

```text
DAO method
→ Database.getInstance().getConnection()
→ execute SQL
→ close connection automatically by try-with-resources
```

This is safer for SQLite and avoids sharing one mutable connection across many server threads.

### Related files

```text
server/src/main/java/com/auction/server/dao/Database.java
server/src/main/java/com/auction/server/dao/SchemaInitializer.java
server/src/main/java/com/auction/server/config/AppProperties.java
server/src/main/resources/application.properties
```

### Example usage

```java
try (Connection connection = Database.getInstance().getConnection()) {
    // Execute SQL here
}
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

### Example mapping

```text
ItemType.ELECTRONICS → new Electronics(...)
ItemType.ART         → new Art(...)
ItemType.VEHICLE     → new Vehicle(...)
```

### Why Factory Method is appropriate here

Without a factory, object creation logic would be duplicated in controllers, services, and tests.

Bad approach:

```java
if (type == ItemType.ELECTRONICS) {
    return new Electronics(...);
} else if (type == ItemType.ART) {
    return new Art(...);
} else if (type == ItemType.VEHICLE) {
    return new Vehicle(...);
}
```

Better approach:

```java
Item item = itemFactory.create(data);
```

Benefits:

- `ItemService` does not need to know all subclass constructors.
- Adding a new item type later is easier.
- The design demonstrates abstraction and polymorphism.
- The code is easier to test.
- Business services stay cleaner.

### Related files

```text
common/src/main/java/com/auction/common/model/Item.java
common/src/main/java/com/auction/common/model/Electronics.java
common/src/main/java/com/auction/common/model/Art.java
common/src/main/java/com/auction/common/model/Vehicle.java
common/src/main/java/com/auction/common/enums/ItemType.java
server/src/main/java/com/auction/server/factory/ItemFactory.java
server/src/test/java/com/auction/server/factory/ItemFactoryTest.java
```

### Expected usage in service layer

Later, `ItemService` should use `ItemFactory` like this:

```java
Item item = itemFactory.create(new ItemFactory.CreateItemData(
        0L,
        sellerId,
        request.itemType(),
        request.name(),
        request.description(),
        request.startingPrice(),
        request.imagePath(),
        request.brand(),
        request.model(),
        request.artist(),
        request.material(),
        request.manufacturer(),
        request.year(),
        LocalDateTime.now()
));
```

Then `ItemService` passes the item to `ItemDao` for persistence.

---

## 3. Observer

### Planned implementation

Observer will be implemented in the realtime bidding feature.

Expected classes:

```text
server/src/main/java/com/auction/server/observer/AuctionObserver.java
server/src/main/java/com/auction/server/observer/AuctionSubject.java
server/src/main/java/com/auction/server/service/BroadcastService.java
server/src/main/java/com/auction/server/socket/ClientConnectionRegistry.java
server/src/main/java/com/auction/server/socket/ClientHandler.java
```

### Purpose

When a valid bid is placed, all clients watching the same auction must receive an update immediately.

Expected flow:

```text
BidService.placeBid()
→ auctionDao.updateAfterBid(...)
→ bidDao.insert(...)
→ BroadcastService.broadcastBidUpdate(...)
→ ClientHandler.sendBroadcast(...)
→ JavaFX Client receives BID_UPDATE
→ Platform.runLater(...) updates UI
```

### Why Observer is appropriate here

- Multiple clients can watch the same auction.
- The server does not need to know how each client displays the update.
- Clients can subscribe or unsubscribe from auction events.
- Realtime update is required without polling.
- It separates bidding logic from notification logic.

### Expected event types

```text
BID_UPDATE
AUCTION_CLOSED
TIME_EXTENDED
```

### Expected subscription flow

```text
Client opens LiveBiddingView
→ sends SUBSCRIBE_AUCTION
→ server registers ClientHandler as watcher
→ new bid happens
→ server broadcasts BID_UPDATE to all watchers
```

When the user leaves the screen:

```text
Client closes LiveBiddingView
→ sends UNSUBSCRIBE_AUCTION
→ server removes ClientHandler from watcher list
```

### Client-side rule

JavaFX UI must not be updated directly from the socket listener thread.

Correct:

```java
Platform.runLater(() -> {
    currentPriceLabel.setText(formatMoney(event.amount()));
    bidHistory.add(0, event.toBidDto());
});
```

Incorrect:

```java
currentPriceLabel.setText(formatMoney(event.amount())); // wrong if called from socket thread
```

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

### Purpose

Different bidding methods can share one interface but have different algorithms.

Manual bidding:

```text
User enters amount manually
→ server validates amount
→ server places bid
```

Auto bidding:

```text
User sets maxBid and increment
→ server automatically increases bid when outbid
→ server never exceeds maxBid
```

### When to implement

Only implement Strategy after the required core system is stable:

```text
AuctionService
BidService
BidDao
Realtime update
Concurrent bidding test
```

---

## 5. Command - Optional

### Possible usage

Command can be used to encapsulate bid actions:

```text
BidCommand
```

A bid command could contain:

```text
auctionId
bidderId
amount
timestamp
execute()
```

### Why it might help

- Encapsulates bid action as an object.
- Easier to log or replay commands.
- Could support undo/rollback in a more advanced system.

### Project decision

This pattern is optional and should not be prioritized before required features.

---

## 6. Pattern Summary

| Pattern | Status | Main class | Purpose |
|---|---|---|---|
| Singleton | Implemented | `Database` | Centralized database connection configuration |
| Factory Method | Implemented | `ItemFactory` | Create concrete `Item` subclasses |
| Observer | Planned W7/W10 | `BroadcastService`, `AuctionObserver` | Realtime bid update |
| Strategy | Optional | `BidStrategy` | Manual bid vs auto bid |
| Command | Optional | `BidCommand` | Encapsulate bid action |

---

## 7. Explanation for Presentation

During the final presentation, the team can explain the required patterns like this:

### Singleton explanation

`Database` is a Singleton because all DAO classes need one shared database manager. This keeps SQLite configuration consistent and prevents each DAO from configuring connections differently.

### Factory Method explanation

`ItemFactory` is used because `Item` is abstract and the system has multiple concrete item types. Instead of writing object creation logic in many services, the factory creates the correct subclass based on `ItemType`.

### Observer explanation

Observer is used for realtime bidding. When one client places a valid bid, the server notifies every client currently watching the auction. This avoids polling and keeps all bidding screens synchronized.

---

## 8. Notes for Team Members

- Do not create `Electronics`, `Art`, or `Vehicle` directly in controllers.
- Use `ItemFactory` when creating item domain objects.
- Do not access `Database` from the client module.
- Keep realtime notification logic outside `BidService` as much as possible.
- Use `Platform.runLater()` for all JavaFX UI updates triggered by socket events.
