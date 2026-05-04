package com.auction.common.model;

import com.auction.common.enums.Role;
import java.time.LocalDateTime;

/**
 * Admin can manage users and auctions.
 */
public class Admin extends User {

    public Admin(
        long id,
        String username,
        String passwordHash,
        String fullName,
        boolean active,
        LocalDateTime createdAt
    ) {
        super(
            id,
            username,
            passwordHash,
            fullName,
            Role.ADMIN,
            active,
            createdAt
        );
    }

    public boolean canManageSystem() {
        return isActive();
    }

    @Override
    public String displayName() {
        return "Admin: " + getFullName();
    }
}
