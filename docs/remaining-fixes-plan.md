# Plan fix loi con ton dong

Ngay 2026-05-16, sau khi bo sung lifecycle dau gia `OPEN -> RUNNING -> FINISHED -> PAID / CANCELED`, cac loi/rui ro con lai nen xu ly theo thu tu sau:

## P0 - Wallet va bid phai co tinh atomic

Van de: `BidService.placeBid` dang phoi hop nhieu thao tac doc/ghi auction, bid va wallet. Neu mot thao tac thanh cong nhung thao tac sau do loi, so du, locked balance, current price hoac highest bidder co the lech nhau.

Huong fix:
- Gom cap nhat bid, auction va wallet vao mot transaction database duy nhat.
- Neu chua the transaction hoa ngay, doi thu tu xu ly sang lock tien nguoi moi truoc, chi release tien nguoi cu sau khi auction update thanh cong.
- Bo sung rollback/compensation ro rang khi wallet update loi.
- Them test fail giua cac buoc lock, update auction, insert bid, release old max bid.

## P0 - Retry settlement cho dau gia FINISHED

Van de: `FINISHED` hien la trang thai trung gian cho phien da het gio nhung dang chot thanh toan. Neu settle wallet loi tam thoi, can co co che retry va hien thi ro ly do.

Huong fix:
- Luu settlement attempt, last error va timestamp trong bang rieng hoac them cot metadata.
- Cho `AuctionManagerService` retry `FINISHED -> PAID` theo backoff.
- Neu qua so lan retry, day thong bao admin/seller va giu `FINISHED` de tranh mat tien.
- Them test settlement loi lan dau, thanh cong lan sau.

## P1 - Tach RequestRouter thanh cac handler nho

Van de: `RequestRouter` dang la lop dieu phoi lon, gom nhieu nghiep vu auth, auction, bid, seller stats va admin. Moi thay doi nho co blast radius cao.

Huong fix:
- Tach `AuctionRequestHandler`, `BidRequestHandler`, `SellerRequestHandler`, `AdminRequestHandler`.
- Giu `RequestRouter` chi con map `MessageType -> handler`.
- Them test routing de dam bao message cu van tra response cu.

## P1 - Chuan hoa status va UI labels

Van de: code dang song song `CANCELED` trong enum va `status-cancelled` trong CSS legacy. Dieu nay de gay sai style khi them status moi.

Huong fix:
- Tao helper dung chung de map `AuctionStatus -> cssClass`.
- Thay cac switch trung lap trong controller bang helper.
- Cap nhat docs user/admin de mo ta ro `FINISHED` la pending settlement, `PAID` la thanh toan xong.

## P1 - Session va token hardening

Van de: token-based auth hien con don gian, can ro TTL, invalidate va tranh leak token trong log.

Huong fix:
- Them thoi han token va logout invalidation.
- Audit log de khong in token/raw credentials.
- Them test request token het han va token sai role.

## P2 - Exception domain rieng

Van de: nhieu loi nghiep vu dang dung `IllegalArgumentException`/`IllegalStateException`, khien client kho phan loai thong bao.

Huong fix:
- Them exception rieng: `InvalidBidException`, `InsufficientFundsException`, `AuctionClosedException`, `UnauthorizedActionException`.
- Map exception sang response code/message on dinh trong socket layer.
- Them test message loi cho client.

## P2 - Regression test UI va realtime

Van de: cac luong realtime nhu `AUCTION_CLOSED`, `BID_UPDATE`, `TIME_EXTENDED` kho bat loi bang unit test backend don le.

Huong fix:
- Lap manual regression checklist cho Bidder, Seller, Admin.
- Bo sung test tich hop socket neu kip: server local + client socket gia lap.
- Kiem thu cac case: reserve met, reserve not met, no bid, paid success, settlement retry, canceled by seller/admin.
