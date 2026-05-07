package com.auction.common.dto.bid;

import java.math.BigDecimal;

/**
 * Payload for PLACE_BID request.
 */
public record PlaceBidRequest(Long auctionId, BigDecimal amount) {}
