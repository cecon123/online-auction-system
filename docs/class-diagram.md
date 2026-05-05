# Class Diagram - Online Auction System

This document describes the main domain model of the Online Auction System.

## 1. Main Inheritance Tree

```text
Entity (abstract)
├── User (abstract)
│   ├── Bidder
│   ├── Seller
│   └── Admin
│
├── Item (abstract)
│   ├── Electronics
│   ├── Art
│   └── Vehicle
│
├── Auction
├── BidTransaction
└── AutoBidRule
```

## 2. User Hierarchy

```text
User
├── id
├── username
├── passwordHash
├── fullName
├── role
├── active
└── createdAt
```

Subclasses:

```text
Bidder
- canBid()

Seller
- canCreateAuction()

Admin
- canManageSystem()
```

Reason:

- `User` is abstract because the system does not use a generic user directly.
- `Bidder`, `Seller`, and `Admin` have different permissions.
- This demonstrates inheritance, abstraction, and polymorphism.

## 3. Item Hierarchy

```text
Item
├── id
├── sellerId
├── itemType
├── name
├── description
├── condition      # Added in W6 sync
├── startingPrice
├── imagePath
└── createdAt
```

Subclasses:

```text
Electronics
- brand
- model

Art
- artist
- material

Vehicle
- manufacturer
- year
```

Reason:

- `Item` is abstract because each product belongs to a concrete category.
- Subclasses override `categoryDescription()`.
- This demonstrates polymorphism.

## 4. Auction

```text
Auction
├── id
├── itemId
├── sellerId
├── currentPrice
├── highestBidderId
├── startTime
├── endTime
├── status
├── version        # For optimistic locking if needed
└── createdAt
```

Important methods:

```text
isRunningAt(now)
canAcceptBidAt(now)
updateHighestBid(bidderId, amount)
extendEndTimeSeconds(seconds)
```

State flow:

```text
OPEN → RUNNING → FINISHED → PAID
                    └──────→ CANCELED
```

## 5. Service & Concurrency Layer (Planned W7)

To handle concurrent bidding and complex business logic, the server uses a Service Layer:

- **AuctionLockManager**: Manages `ReentrantLock` instances per `auctionId` to ensure thread-safety during bidding.
- **LockRegistry**: A registry to track and reuse locks efficiently.
- **BidService**: Processes placing bids, validating rules, updating auction state, and recording transactions.
- **AuctionService**: Manages auction lifecycle (opening, closing, anti-sniping).
- **AuthService**: Handles registration, login (BCrypt), and session management.

## 6. BidTransaction

```text
BidTransaction
├── id
├── auctionId
├── bidderId
├── amount
└── createdAt
```

Reason:

- Every accepted bid is stored as a transaction.
- Bid history is generated from this model.

## 6. AutoBidRule

```text
AutoBidRule
├── id
├── auctionId
├── bidderId
├── maxBid
├── increment
├── active
└── createdAt
```

This is for optional Auto-Bidding.

## 7. Relationship Summary

```text
Seller 1 ---- * Item
Seller 1 ---- * Auction
Item   1 ---- 1 Auction
Auction 1 --- * BidTransaction
Bidder 1 ---- * BidTransaction
Auction 1 --- * AutoBidRule
Bidder 1 ---- * AutoBidRule
```

## 8. Design Patterns Related to Model

### Factory Method

`ItemFactory` will create concrete item objects:

```text
ItemFactory.create(...)
├── Electronics
├── Art
└── Vehicle
```

### Observer

`Auction` updates will be broadcast to subscribed clients through:

```text
BidService
→ BroadcastService
→ ClientHandler
→ JavaFX Client
```

### Singleton

Database connection manager:

```text
Database.getInstance()
```

## 9. Notes for Team Members

- Client must not access DAO or SQLite directly.
- Server services use model classes to apply business logic.
- DTO classes are used only for socket JSON transfer.
- DAO records map database rows to Java objects.

## 10. Suggested Mermaid Class Diagram

Paste this block into a Mermaid renderer or supported Markdown viewer if a visual diagram is needed.

```mermaid
classDiagram
    class Entity {
        <<abstract>>
        -long id
        -LocalDateTime createdAt
        +long getId()
        +void setId(long id)
        +LocalDateTime getCreatedAt()
        +void setCreatedAt(LocalDateTime createdAt)
        +String displayName()*
    }

    class User {
        <<abstract>>
        -String username
        -String passwordHash
        -String fullName
        -Role role
        -boolean active
        +String getUsername()
        +String getPasswordHash()
        +String getFullName()
        +Role getRole()
        +boolean isActive()
        +void activate()
        +void deactivate()
        +boolean hasRole(Role role)
    }

    class Bidder {
        +boolean canBid()
        +String displayName()
    }

    class Seller {
        +boolean canCreateAuction()
        +String displayName()
    }

    class Admin {
        +boolean canManageSystem()
        +String displayName()
    }

    class Item {
        <<abstract>>
        -long sellerId
        -ItemType itemType
        -String name
        -String description
        -String condition
        -BigDecimal startingPrice
        -String imagePath
        +String getName()
        +BigDecimal getStartingPrice()
        +String displayName()
        +String categoryDescription()*
    }

    class Electronics {
        -String brand
        -String model
        +String categoryDescription()
    }

    class Art {
        -String artist
        -String material
        +String categoryDescription()
    }

    class Vehicle {
        -String manufacturer
        -int year
        +String categoryDescription()
    }

    class Auction {
        -long itemId
        -long sellerId
        -BigDecimal currentPrice
        -Long highestBidderId
        -LocalDateTime startTime
        -LocalDateTime endTime
        -AuctionStatus status
        -long version
        +boolean isRunningAt(LocalDateTime now)
        +boolean canAcceptBidAt(LocalDateTime now)
        +void updateHighestBid(long bidderId, BigDecimal amount)
        +void extendEndTimeSeconds(long seconds)
        +String displayName()
    }

    class BidTransaction {
        -long auctionId
        -long bidderId
        -BigDecimal amount
        +String displayName()
    }

    class AutoBidRule {
        -long auctionId
        -long bidderId
        -BigDecimal maxBid
        -BigDecimal increment
        -boolean active
        +boolean canBidUpTo(BigDecimal amount)
        +void deactivate()
        +String displayName()
    }

    Entity <|-- User
    User <|-- Bidder
    User <|-- Seller
    User <|-- Admin

    Entity <|-- Item
    Item <|-- Electronics
    Item <|-- Art
    Item <|-- Vehicle

    Entity <|-- Auction
    Entity <|-- BidTransaction
    Entity <|-- AutoBidRule

    Seller "1" --> "*" Item : creates
    Seller "1" --> "*" Auction : owns
    Item "1" --> "1" Auction : listed_in
    Auction "1" --> "*" BidTransaction : has
    Bidder "1" --> "*" BidTransaction : places
    Auction "1" --> "*" AutoBidRule : has
    Bidder "1" --> "*" AutoBidRule : configures
```
