package com.ums.security.audit.application;

import com.ums.common.audit.EntityChangedEvent;
import com.ums.security.audit.api.dto.AuditLogRequest;
import com.ums.security.audit.api.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** The audit log: writes one row for each change to an @Auditable entity, and lets admins search them. */
public interface AuditLogService {

    /** Saves one audit_logs row for a change to an @Auditable entity. */
    void record(EntityChangedEvent event);

    /**
     * One page of audit rows for the admin history page, newest first by default. Every filter is optional.
     *
     * @param filter   userId, entity (e.g. Person), entityId, action (CREATE / UPDATE / DELETE), from / to (days, both included)
     * @param pageable page number, size and sort
     * @return one page of audit rows, with the totals
     */
    Page<AuditLogResponse> list(AuditLogRequest filter, Pageable pageable);
}
