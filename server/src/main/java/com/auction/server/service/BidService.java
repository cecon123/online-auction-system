package com.auction.server.service;

import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.concurrency.AuctionLockManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service xử lý các nghiệp vụ liên quan đến đặt giá (Bidding).
 * Đây là nơi áp dụng cơ chế Locking để đảm bảo tính thread-safe.
 */
public class BidService {

    private final AuctionLockManager lockManager;

    public BidService() {
        this.lockManager = AuctionLockManager.getInstance();
    }

    /**
     * Xử lý yêu cầu đặt giá từ người dùng.
     *
     * @param bidderId ID của người đặt giá.
     * @param request  Thông tin đặt giá (auctionId, amount).
     * @return PlaceBidResponse nếu thành công.
     * @throws IllegalArgumentException nếu bid không hợp lệ.
     */
    public PlaceBidResponse placeBid(long bidderId, PlaceBidRequest request) {
        return lockManager.executeLocked(request.auctionId(), () -> {
            // TODO: Sau này sẽ gọi AuctionDao để lấy dữ liệu thật từ DB
            // Hiện tại giả lập logic kiểm tra:

            BigDecimal currentPrice = new BigDecimal("1000.00"); // Mock
            LocalDateTime endTime = LocalDateTime.now().plusHours(1); // Mock
            AuctionStatus status = AuctionStatus.RUNNING; // Mock

            // 1. Kiểm tra trạng thái phiên
            if (status != AuctionStatus.RUNNING) {
                throw new IllegalArgumentException("Phiên đấu giá không trong trạng thái cho phép đặt giá.");
            }

            // 2. Kiểm tra thời gian
            if (LocalDateTime.now().isAfter(endTime)) {
                throw new IllegalArgumentException("Phiên đấu giá đã kết thúc.");
            }

            // 3. Kiểm tra giá đặt
            if (request.amount().compareTo(currentPrice) <= 0) {
                throw new IllegalArgumentException("Giá đặt phải cao hơn giá hiện tại.");
            }

            // 4. TODO: Cập nhật DB qua AuctionDao và ghi log qua BidDao

            return new PlaceBidResponse(
                request.auctionId(),
                request.amount(),
                "user-" + bidderId, // Mock username
                LocalDateTime.now()
            );
        });
    }
}
