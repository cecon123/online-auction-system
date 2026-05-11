package com.auction.common.dto.auction;

import com.auction.common.enums.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for auction events like status changes or time extensions. */
public record AuctionEventDto(
    Long auctionId,
    AuctionStatus status,
    String message,
    String winnerUsername,
    BigDecimal finalPrice,
    LocalDateTime newEndTime) {}
