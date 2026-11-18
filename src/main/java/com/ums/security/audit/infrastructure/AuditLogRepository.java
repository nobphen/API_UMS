package com.ums.security.audit.infrastructure;

import com.ums.security.audit.domain.AuditLog;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for the audit_logs table: save (AuditLogServiceImpl.record) and the admin search. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Audit rows for the admin history page. Each filter that is null is not used.
     * from / to are never null: the service sends a very old / far future date when the admin leaves them empty
     * (PostgreSQL can't tell the type of a null date parameter: "could not determine data type of parameter").
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=createdAt,desc).
     * <pre>
     * SELECT * FROM audit_logs
     * WHERE (? IS NULL OR user_id = ?) AND (? IS NULL OR entity = ?)
     *   AND (? IS NULL OR entity_id = ?) AND (? IS NULL OR action = ?)
     *   AND created_at >= ? AND created_at < ?
     * ORDER BY created_at DESC LIMIT ? OFFSET ?
     * </pre>
     * The index ix_audit_entity (entity, entity_id) makes "the history of person 1" fast.
     *
     * @param userId   who made the change, or null for everyone
     * @param entity   the Java class name, e.g. Person, Student, StudentGuardian (case-sensitive), or null for all
     * @param entityId the id of the changed row, or null for all
     * @param action   CREATE, UPDATE or DELETE, or null for all
     * @param from     first moment included (start of the "from" day)
     * @param to       first moment NOT included (start of the day after "to")
     * @param pageable page number, size and sort
     * @return one page of audit rows, with the totals
     */
    @Query("""
            select a from AuditLog a
            where (:userId is null or a.userId = :userId)
              and (:entity is null or a.entity = :entity)
              and (:entityId is null or a.entityId = :entityId)
              and (:action is null or a.action = :action)
              and a.createdAt >= :from
              and a.createdAt < :to
            """)
    Page<AuditLog> search(Long userId, String entity, Long entityId, String action,
                          OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
