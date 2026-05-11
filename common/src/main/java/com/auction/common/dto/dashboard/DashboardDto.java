package com.auction.common.dto.dashboard;

import java.math.BigDecimal;

/** Data Transfer Object for dashboard statistics. The content varies based on the user's role. */
public record DashboardDto(
    BigDecimal balance,
    BigDecimal lockedBalance,
    int participatingAuctionsCount, // Number of auctions the bidder is participating in
    int winningAuctionsCount, // Number of auctions where the bidder is currently the highest bidder
    int activeAuctionsCount, // Number of auctions currently RUNNING (for Seller/Admin)
    int totalAuctionsCount, // Total number of auctions (for Seller/Admin)
    int totalUsersCount // Total number of registered users (for Admin)
    ) {}
