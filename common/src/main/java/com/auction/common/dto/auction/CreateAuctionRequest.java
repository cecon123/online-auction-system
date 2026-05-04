package com.auction.common.dto.auction;

import com.auction.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionRequest(
        String itemName,
        ItemType itemType,
        String description,
        BigDecimal startingPrice,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String imagePath
) {
}
