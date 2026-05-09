package com.auction.common.dto.bid;

import java.math.BigDecimal;

/**
 * Data Transfer Object for auto-bid rule information.
 */
public record AutoBidDto(
    long auctionId,
    BigDecimal maxBid,
    BigDecimal increment,
    boolean active
) {}
