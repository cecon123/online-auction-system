package com.auction.common.dto.auth;

import com.auction.common.enums.Role;

public record RegisterResponse(
    long userId,
    String username,
    Role role,
    java.math.BigDecimal balance,
    java.math.BigDecimal lockedBalance) {}
