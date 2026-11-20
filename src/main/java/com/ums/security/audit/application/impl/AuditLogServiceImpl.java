package com.ums.security.audit.application.impl;

import com.ums.common.audit.EntityChangedEvent;
import com.ums.common.util.DateUtils;
import com.ums.common.util.Texts;
import com.ums.security.audit.api.dto.AuditLogRequest;
import com.ums.security.audit.api.dto.AuditLogResponse;
import com.ums.security.audit.application.AuditLogService;
import com.ums.security.audit.application.mapper.AuditLogMapper;
import com.ums.security.audit.domain.AuditLog;
import com.ums.security.audit.infrastructure.AuditLogRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/** Listens for EntityChangedEvent and saves each change as an audit_logs row; searches them for the admin. */
@Service
@Transactional
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    /** Used when the admin leaves "from" empty: older than any audit row. */
    private static final LocalDate FIRST_DAY = LocalDate.of(2000, 1, 1);

    /** Used when the admin leaves "to" empty: later than any audit row. */
    private static final LocalDate LAST_DAY = LocalDate.of(2100, 1, 1);

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;   // AuditLog → AuditLogResponse (MapStruct)

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

    /**
     * Days → exact moments (never null, see AuditLogRepository.search) → one page → responses.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> list(AuditLogRequest filter, Pageable pageable) {

        // 1. from: empty → a very old day; the search starts at 00:00 of that day
        //    from=2026-11-18 → 2026-11-18T00:00+07:00
        LocalDate fromDay = filter.from() == null ? FIRST_DAY : filter.from();
        OffsetDateTime from = DateUtils.startOfDay(fromDay);   // the school's day: 00:00 in Cambodia, not in UTC

        // 2. to: empty → far in the future; the search stops BEFORE 00:00 of the NEXT day,
        //    so the whole "to" day is included: to=2026-11-18 → < 2026-11-19T00:00+07:00
        LocalDate toDay = filter.to() == null ? LAST_DAY : filter.to();
        OffsetDateTime to = DateUtils.startOfDay(toDay.plusDays(1));

        // 3. One page from the database (the other filters: null = all), each row → response
        return auditLogRepository.search(
                        filter.userId(),
                        Texts.blankToNull(filter.entity()),
                        filter.entityId(),
                        Texts.blankToNull(filter.action()),
                        from,
                        to,
                        pageable
                ).map(auditLogMapper::toResponse);
    }
}
