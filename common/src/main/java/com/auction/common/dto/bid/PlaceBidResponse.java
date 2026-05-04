package com.auction.common.dto.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload returned when a bid is accepted.
 */
public record PlaceBidResponse(
    long auctionId,
    BigDecimal currentPrice,
    String highestBidderUsername,
    LocalDateTime timestamp
) {}
