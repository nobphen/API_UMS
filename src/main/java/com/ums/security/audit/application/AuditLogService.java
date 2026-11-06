package com.ums.security.audit.application;

import com.ums.common.audit.EntityChangedEvent;

/** Writes the audit log: one row for each change to an @Auditable entity. */
public interface AuditLogService {

    /** Saves one audit_logs row for a change to an @Auditable entity. */
    void record(EntityChangedEvent event);
}
