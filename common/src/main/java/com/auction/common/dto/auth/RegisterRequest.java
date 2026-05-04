package com.auction.common.dto.auth;

import com.auction.common.enums.Role;

public record RegisterRequest(
        String fullName,
        String username,
        String password,
        Role role
) {
}
