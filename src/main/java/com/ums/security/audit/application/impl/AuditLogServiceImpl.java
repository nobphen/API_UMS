package com.ums.security.audit.application.impl;

import com.ums.common.audit.EntityChangedEvent;
import com.ums.security.audit.application.AuditLogService;
import com.ums.security.audit.domain.AuditLog;
import com.ums.security.audit.infrastructure.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Runs after the change is committed, in its own transaction: a rolled-back change leaves no audit row,
     * and a failing audit row never undoes the change.
     */
    @Override
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(EntityChangedEvent event) {
        AuditLog log = new AuditLog();
        log.setUserId(event.userId());
        log.setAction(event.action().name());
        log.setEntity(event.entity());
        log.setEntityId(event.entityId());
        log.setOldValue(event.oldValue());
        log.setNewValue(event.newValue());
        log.setIpAddress(event.ipAddress());
        auditLogRepository.save(log);
    }
}
