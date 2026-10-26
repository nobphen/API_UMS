package com.ums.common.audit;

/**
 * Sent by {@link AuditLogListener} when an {@link Auditable} entity is saved or deleted. The security module
 * listens for it and writes the audit_logs row, so common does not depend on security.
 *
 * @param userId   the logged-in user; null for system jobs
 * @param action   CREATE, UPDATE or DELETE
 * @param entity   the entity's class name, e.g. Invoice
 * @param oldValue JSON of the changed fields before (null for CREATE)
 * @param newValue JSON of the changed fields after (null for DELETE)
 */
public record EntityChangedEvent(
        Long userId,
        Action action,
        String entity,
        Long entityId,
        String oldValue,
        String newValue,
        String ipAddress) {

    public enum Action { CREATE, UPDATE, DELETE }
}
