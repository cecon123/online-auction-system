package com.auction.common.dto.dashboard;

import java.math.BigDecimal;

/** Data Transfer Object for seller statistics in the Seller Dashboard. */
public record SellerStatsDto(
    BigDecimal expectedRevenue, // Total of current highest bids on active/pending auctions
    BigDecimal totalRevenue, // Total revenue from successful (FINISHED) auctions
    int totalBidsReceived, // Total number of bids placed on this seller's auctions
    int successRate, // Percentage of FINISHED successful auctions vs total closed auctions
    int activeAuctionsCount, // Number of active auctions
    int totalAuctionsCount // Total number of auctions created
    ) {}
