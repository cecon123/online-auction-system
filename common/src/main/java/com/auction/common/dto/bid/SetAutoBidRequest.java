package com.auction.common.dto.bid;

import java.math.BigDecimal;

/** Request to set or update an auto-bid rule. */
public record SetAutoBidRequest(
    long auctionId, BigDecimal maxBid, BigDecimal increment, boolean active) {}
