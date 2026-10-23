package com.ums.security.audit.api.dto;

import java.time.LocalDate;

/** Search filter for the audit log (rows are written by the system, never by clients); every field is optional. */
public record AuditLogRequest(
        Long userId,
        String entity,
        Long entityId,
        String action,
        LocalDate from,
        LocalDate to) {
}
