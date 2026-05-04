package com.auction.common.dto.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidUpdateEvent(
        long auctionId,
        String bidderUsername,
        BigDecimal amount,
        LocalDateTime timestamp,
        LocalDateTime newEndTime
) {
}
