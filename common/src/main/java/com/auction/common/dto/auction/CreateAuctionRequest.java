package com.auction.common.dto.auction;

import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload for seller creating a new auction.
 *
 * This DTO is aligned with CreateAuctionView.fxml:
 * - item name
 * - item type
 * - condition
 * - description
 * - starting price
 * - start/end time
 * - image path
 */
public record CreateAuctionRequest(
    String itemName,
    ItemType itemType,
    String condition,
    String description,
    BigDecimal startingPrice,
    BigDecimal reservePrice,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String imagePath,
    String imageBase64
) {}
