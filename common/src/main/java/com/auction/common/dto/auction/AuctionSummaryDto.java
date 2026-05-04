package com.auction.common.dto.auction;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionSummaryDto(
        long id,
        String title,
        ItemType itemType,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AuctionStatus status,
        String imagePath
) {
}
