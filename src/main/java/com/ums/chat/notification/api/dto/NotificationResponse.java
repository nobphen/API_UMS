package com.ums.chat.notification.api.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String type,
        String title,
        String message,
        String link,
        boolean read,
        OffsetDateTime createdAt) {
}
