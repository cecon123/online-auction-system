package com.auction.common.model;

import com.auction.common.enums.Role;
import java.time.LocalDateTime;

/**
 * Bidder can participate in auctions and place bids.
 */
public class Bidder extends User {

    public Bidder(
        long id,
        String username,
        String passwordHash,
        String fullName,
        java.math.BigDecimal balance,
        boolean active,
        LocalDateTime createdAt
    ) {
        super(
            id,
            username,
            passwordHash,
            fullName,
            Role.BIDDER,
            balance,
            active,
            createdAt
        );
    }

    public boolean canBid() {
        return isActive();
    }

    @Override
    public String displayName() {
        return "Bidder: " + getFullName();
    }
}
