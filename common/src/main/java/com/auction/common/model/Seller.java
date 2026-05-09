package com.auction.common.model;

import com.auction.common.enums.Role;
import java.time.LocalDateTime;

/**
 * Seller can create items and auctions.
 */
public class Seller extends User {

    public Seller(
        long id,
        String username,
        String passwordHash,
        String fullName,
        java.math.BigDecimal balance,
        java.math.BigDecimal lockedBalance,
        boolean active,
        LocalDateTime createdAt
    ) {
        super(
            id,
            username,
            passwordHash,
            fullName,
            Role.SELLER,
            balance,
            lockedBalance,
            active,
            createdAt
        );
    }

    public boolean canCreateAuction() {
        return isActive();
    }

    @Override
    public String displayName() {
        return "Seller: " + getFullName();
    }
}
