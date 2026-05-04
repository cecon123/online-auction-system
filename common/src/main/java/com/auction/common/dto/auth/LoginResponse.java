package com.auction.common.dto.auth;

import com.auction.common.enums.Role;

/**
 * Payload returned after successful login.
 */
public record LoginResponse(
    long userId,
    String username,
    Role role,
    String token
) {}
