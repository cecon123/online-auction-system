# Socket JSON Protocol

All client-server messages are newline-delimited JSON strings.

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
    "highestBidderUsername": "huy"
  }
}
```

## Realtime Event Format

```
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

## Message Types
- REGISTER
- LOGIN
- LOGOUT
- GET_DASHBOARD
- GET_AUCTIONS
- GET_AUCTION_DETAIL
- CREATE_ITEM
- UPDATE_ITEM
- DELETE_ITEM
- CREATE_AUCTION
- UPDATE_AUCTION
- CANCEL_AUCTION
- PLACE_BID
- GET_BID_HISTORY
- SUBSCRIBE_AUCTION
- UNSUBSCRIBE_AUCTION
- BID_UPDATE
- AUCTION_CLOSED
- TIME_EXTENDED
- ADMIN_GET_USERS
- ADMIN_UPDATE_USER_STATUS
- ADMIN_GET_AUCTIONS
