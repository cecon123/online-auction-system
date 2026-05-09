package com.auction.common.dto.auction;

import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed auction data used by AuctionDetailView and LiveBiddingView.
 */
public record AuctionDetailDto(
    long auctionId,
    long itemId,
    long sellerId,
    String sellerUsername,
    String title,
    ItemType itemType,
    String condition,
    String description,
    BigDecimal startingPrice,
    BigDecimal currentPrice,
    BigDecimal reservePrice,
    String highestBidderUsername,
    LocalDateTime startTime,
    LocalDateTime endTime,
    AuctionStatus status,
    String imagePath
) {}
