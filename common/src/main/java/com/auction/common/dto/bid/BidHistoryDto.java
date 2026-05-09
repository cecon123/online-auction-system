package com.auction.common.dto.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for a user's bid history entry.
 */
public record BidHistoryDto(
    long bidId,
    long auctionId,
    String auctionTitle,
    BigDecimal amount,
    LocalDateTime timestamp,
    String result // e.g., "WINNING", "OUTBID", "WON", "LOST"
) {}
