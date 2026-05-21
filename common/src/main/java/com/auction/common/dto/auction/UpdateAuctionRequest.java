package com.auction.common.dto.auction;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload for seller updating an existing auction. Only allowed when auction has no bids and is
 * OPEN or RUNNING.
 */
public record UpdateAuctionRequest(
    long auctionId,
    String itemName,
    ItemType itemType,
    String condition,
    String description,
    BigDecimal startingPrice,
    BigDecimal reservePrice,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String imagePath,
    String imageBase64) {}
