package com.auction.common.dto.auth;

/** Payload for LOGIN request. */
public record LoginRequest(String username, String password) {}
