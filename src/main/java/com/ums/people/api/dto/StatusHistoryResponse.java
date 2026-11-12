package com.ums.people.api.dto;

import java.time.OffsetDateTime;

/**
 * One status change sent back to the client, for staff and students alike.
 * The statuses are sent as text (ACTIVE, ON_LEAVE, ...), so one response fits both.
 */
public record StatusHistoryResponse(
        Long id,
        String oldStatus,
        String newStatus,
        String reason,
        Long changedBy,          // users.id of the admin
        OffsetDateTime changedAt) {
}
