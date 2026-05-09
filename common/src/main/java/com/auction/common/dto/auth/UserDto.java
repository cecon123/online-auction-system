package com.auction.common.dto.auth;

import com.auction.common.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserDto(
    long id,
    String username,
    String fullName,
    Role role,
    BigDecimal balance,
    BigDecimal lockedBalance,
    boolean active,
    LocalDateTime createdAt
) {}
