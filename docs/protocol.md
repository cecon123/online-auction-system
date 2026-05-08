# Socket JSON Protocol

All client-server messages are **newline-delimited JSON** strings. 

> **CRITICAL RULE:** 
> One request = one JSON line. One response = one JSON line.
> Do NOT use pretty printing (multi-line JSON) when sending over the socket, as the `ClientHandler` uses `readLine()` to parse messages.

## Request Format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "token": "session-token",
  "data": {
    "auctionId": 1,
    "amount": 1500000
  }
}
```

- `type`: The `MessageType` enum name.
- `requestId`: A unique identifier (UUID recommended) for the client to track responses.
- `token`: Session token obtained after LOGIN. Null for AUTH requests.
- `data`: Type-specific payload.

## Response Format

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "success": true,
  "message": "Bid accepted",
  "data": {
    "auctionId": 1,
    "currentPrice": 1500000,
    "highestBidderUsername": "huy",
    "timestamp": "2026-05-04T20:30:00"
  }
}
```

- `success`: `true` if processed correctly, `false` otherwise.
- `message`: Human-readable status or error message.

## Error Response Example

```json
{
  "type": "PLACE_BID",
  "requestId": "uuid-123",
  "success": false,
  "message": "Bid amount must be higher than current price",
  "data": null
}
```

## Realtime Event Format (Server Push)

Events pushed by the server have `requestId` as `null`.

```json
{
  "type": "BID_UPDATE",
  "requestId": null,
  "success": true,
  "message": "New bid received",
  "data": {
    "auctionId": 1,
    "bidderUsername": "huy",
    "amount": 1500000,
    "timestamp": "2026-05-04T20:30:00",
    "newEndTime": null
  }
}
```

## Message Types (Implementation Status)

| Category | Type | Purpose |
|---|---|---|
| **AUTH** | `REGISTER` | Create a new user account |
| | `LOGIN` | Authenticate and get session token |
| | `LOGOUT` | Invalidate session |
| **DASHBOARD** | `GET_DASHBOARD` | Get summary for user role |
| **AUCTION** | `GET_AUCTIONS` | List all active/upcoming auctions |
| | `GET_AUCTION_DETAIL` | Detailed info for one auction |
| | `CREATE_AUCTION` | Seller: Create new listing |
| | `UPDATE_AUCTION` | Seller: Edit before RUNNING |
| | `CANCEL_AUCTION` | Seller/Admin: Stop auction |
| **ITEM** | `CREATE_ITEM` | Seller: Create item without auction |
| | `UPDATE_ITEM` | Seller: Edit item details |
| | `DELETE_ITEM` | Seller: Remove item |
| **BID** | `PLACE_BID` | Bidder: Place a new bid |
| | `GET_BID_HISTORY` | View bid logs for an auction |
| **REALTIME** | `SUBSCRIBE_AUCTION` | Listen for updates on an auction |
| | `UNSUBSCRIBE_AUCTION` | Stop listening |
| | `BID_UPDATE` | Server: New bid broadcast |
| | `AUCTION_CLOSED` | Server: Auction ended broadcast |
| | `TIME_EXTENDED` | Server: Anti-sniping trigger broadcast |
| **WALLET** | `DEPOSIT` | Add funds to user balance |
| | `WITHDRAW` | Remove funds from user balance |
| **ADMIN** | `ADMIN_GET_USERS` | List all users |
| | `ADMIN_UPDATE_USER_STATUS` | Enable/Disable users |
| | `ADMIN_GET_AUCTIONS` | System-wide auction management |
