package com.ums.chat.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A notification an admin sends to one user; most notifications are sent by the system itself. */
public record NotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 200) String title,
        String message,
        @Size(max = 500) String link) {
}
