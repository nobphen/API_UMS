package com.ums.security.audit.infrastructure;

import com.ums.security.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for the audit_logs table. Only the built-in JpaRepository methods (save) for now. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
