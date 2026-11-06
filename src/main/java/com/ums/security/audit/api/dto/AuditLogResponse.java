package com.ums.security.audit.api.dto;

import java.time.OffsetDateTime;

/** One audit log row as sent to the client: who did which action to which record, with the values before and after. */
public record AuditLogResponse(
        Long id,
        Long userId,
        String action,
        String entity,
        Long entityId,
        String oldValue,   // JSON
        String newValue,   // JSON
        String ipAddress,
        OffsetDateTime createdAt) {
}
