# Plan fix loi con ton dong

Ngay 2026-05-16, sau khi bo sung lifecycle dau gia `OPEN -> RUNNING -> FINISHED -> PAID / CANCELED`, cac loi/rui ro con lai nen xu ly theo thu tu sau:

## P0 - Wallet va bid phai co tinh atomic - da bo sung guard

Van de: `BidService.placeBid` phoi hop nhieu thao tac doc/ghi auction, bid va wallet. Neu mot thao tac thanh cong nhung thao tac sau do loi, so du, locked balance, current price hoac highest bidder co the lech nhau.

Da lam:
- `BidService.placeBid` chay trong `Database.runInTransaction(...)`, nen lock tien, release tien nguoi cu, update auction va insert bid cung commit/rollback tren cung SQLite connection.
- Thu tu xu ly hien tai lock tien nguoi moi truoc, release tien nguoi cu sau, roi update auction va insert bid.
- Bo sung `BidServiceTransactionTest.shouldRollbackWalletAndAuctionWhenBidPersistenceFails`: ep `BidDao.create` fail sau khi lock/release/update, assert DB rollback ve current price, highest bidder va locked balance ban dau.

Con lai:
- Neu can bao ve side-effect realtime chat hon, co the tach notification manual bid ra sau commit transaction. Hien tai test da bao ve tinh nhat quan DB/wallet/auction.

## P0 - Retry settlement cho dau gia FINISHED - da bo sung guard

Van de: `FINISHED` hien la trang thai trung gian cho phien da het gio nhung dang chot thanh toan. Neu settle wallet loi tam thoi, can co co che retry va hien thi ro ly do.

Da lam:
- Schema `auctions` co `settlement_attempts`, `settlement_last_error`, `settlement_next_retry_at`.
- `AuctionManagerService` retry `FINISHED -> PAID` theo backoff 5s, 15s, 60s, 300s, 900s va giu auction o `FINISHED` khi settlement loi.
- `AuctionManagerServiceTest.shouldKeepFinishedAuctionWhenSettlementFails` assert settlement failure duoc ghi lai va co next retry.
- `AuctionManagerServiceTest.shouldAvoidDuplicatePaidStatusUpdateWhenSettlementSucceeds` assert settlement thanh cong chi persist status hop ly.

Con lai:
- Neu can hien thi ro ly do tren UI admin/seller, them endpoint/DTO expose `settlement_last_error` va `settlement_next_retry_at`.

## P1 - Tach RequestRouter thanh cac handler nho - da xu ly

Van de: `RequestRouter` dang la lop dieu phoi lon, gom nhieu nghiep vu auth, auction, bid, seller stats va admin. Moi thay doi nho co blast radius cao.

Da lam:
- Tach thanh `AuthRequestHandler`, `AuctionRequestHandler`, `BidRequestHandler`, `WalletRequestHandler`, `AdminRequestHandler`, `SubscriptionRequestHandler`.
- `RequestRouter` chi con guard request chung, replay protection, switch `MessageType -> handler`, va exception mapping.
- `RequestRouterAuthorizationTest` bao ve role routing va unsupported legacy message.

Con lai:
- Neu tiep tuc tach nho hon, co the tach seller stats ra `SellerRequestHandler`; hien tai seller stats dang nam trong `AuctionRequestHandler` nhung router da gon va ro trach nhiem hon truoc.

## P1 - Chuan hoa status va UI labels - da xu ly

Van de: code dang song song `CANCELED` trong enum va `status-cancelled` trong CSS legacy. Dieu nay de gay sai style khi them status moi.

Da lam:
- Them `AuctionStatusUi` trong client util de map `AuctionStatus -> cssClass`, badge text va inactive bidding message.
- Controller list/detail/live bidding/admin/seller center dung helper chung thay cho switch lap lai.
- Dung canonical CSS class `status-canceled`; van giu alias `status-cancelled` trong `common.css` de tranh vo style legacy.
- Them `AuctionStatusUiTest` cho canonical canceled class, legacy `CANCELLED` parse va bid-availability guard.

Con lai:
- `MyBidsController` van co mapping rieng cho ket qua bid (`WON`, `OUTBID`, `PENDING PAYMENT`) vi day khong phai mapping truc tiep `AuctionStatus -> cssClass`.

## P1 - Session va token hardening - da xu ly co ban

Van de: token-based auth hien con don gian, can ro TTL, invalidate va tranh leak token trong log.

Da lam:
- `SessionManager` tao token UUID co TTL 2 gio va tu xoa session khi `getUserId(...)` gap token het han.
- `AuthRequestHandler.handleLogout` invalidate token va unregister writer khoi `NotificationService`.
- `SocketClient` chi log tom tat `token=set`/`token=none`, khong in raw token hoac payload credentials.
- Them `SessionManagerTest` cho expired session cleanup va logout invalidation.
- Mo rong `RequestRouterAuthorizationTest` de verify `LOGOUT` lam token khong con dung duoc cho request sau.

Con lai:
- Neu can chinh TTL theo moi truong, co the dua `SESSION_TTL` vao config thay vi hang so 2 gio.
- Chua co refresh token/silent re-authentication; client van yeu cau dang nhap lai sau khi mat ket noi hoac token het han.

## P2 - Exception domain rieng - da xu ly mot phan

Trang thai hien tai: server service/socket layer da co custom exceptions trong `com.auction.server.exception` cho auth, validation, bid, auction, wallet va resource-not-found.

Da lam:
- Them `BusinessException`, `ValidationException`, `AuthenticationException`, `AuthorizationException`, `InvalidBidException`, `AuctionClosedException`, `InsufficientFundsException`, `ResourceNotFoundException`, `BusinessRuleException`.
- `RequestRouter` map `BusinessException` sang `Response.fail(...)` va giu fallback `IllegalArgumentException | IllegalStateException`.
- Unit tests service assert dung exception class cho loi nghiep vu.

Con lai:
- Neu muon client phan loai UI chi tiet hon, can thiet ke them error code trong protocol. Hien tai chua doi JSON contract.

## P2 - Regression test UI va realtime - da bo sung socket tests

Van de: cac luong realtime nhu `AUCTION_CLOSED`, `BID_UPDATE`, `TIME_EXTENDED` kho bat loi bang unit test backend don le.

Da lam:
- Bo sung `ClientHandlerIntegrationTest` cho newline-delimited JSON, subscribe/broadcast `BID_UPDATE`, cleanup khi disconnect.
- Bo sung `SocketClientIntegrationTest` cho response theo `requestId`, event `event-*`, va server-close -> `DISCONNECTED`.

Con lai:
- Lap manual regression checklist cho Bidder, Seller, Admin.
- Kiem thu cac case: reserve met, reserve not met, no bid, paid success, settlement retry, canceled by seller/admin.
