package com.auction.common.dto.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceBidResponse(
        long auctionId,
        BigDecimal currentPrice,
        String highestBidderUsername,
        LocalDateTime timestamp
) {
}
