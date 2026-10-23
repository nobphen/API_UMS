package com.ums.security.audit.api.dto;

import java.time.OffsetDateTime;

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
