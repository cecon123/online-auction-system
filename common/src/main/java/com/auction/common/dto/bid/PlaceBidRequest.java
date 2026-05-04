package com.auction.common.dto.bid;

import java.math.BigDecimal;

public record PlaceBidRequest(
        long auctionId,
        BigDecimal amount
) {
}
