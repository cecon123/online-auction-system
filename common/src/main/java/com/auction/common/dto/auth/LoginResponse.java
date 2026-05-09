package com.auction.common.dto.auth;

import com.auction.common.enums.Role;

/**
 * Payload returned after successful login.
 */
public record LoginResponse(
    long userId,
    String username,
    String fullName,
    Role role,
    java.math.BigDecimal balance,
    String token
) {}
