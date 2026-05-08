package com.auction.common.dto.auth;

public record UpdateUserStatusRequest(
    long userId,
    boolean active
) {}
