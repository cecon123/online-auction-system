package com.auction.common.dto.auction;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lightweight auction data used in auction list cards. */
public record AuctionSummaryDto(
    long id,
    String title,
    ItemType itemType,
    BigDecimal startingPrice,
    BigDecimal currentPrice,
    Long highestBidderId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    AuctionStatus status,
    String imagePath) {}
