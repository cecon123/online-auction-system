package com.auction.common.dto.auth;

public record LoginRequest(
        String username,
        String password
) {
}
