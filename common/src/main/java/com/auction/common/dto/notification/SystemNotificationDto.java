package com.auction.common.dto.notification;

import java.time.LocalDateTime;

public record SystemNotificationDto(
    String title,
    String message,
    String type, // "SUCCESS", "INFO", "WARNING", "ERROR"
    LocalDateTime timestamp
) {}
