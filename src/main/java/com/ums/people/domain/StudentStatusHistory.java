package com.ums.people.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * One status change of a student record, with the reason (table student_status_history).
 * Insert only: a row is never changed or deleted, so the registrar can always read the full story.
 */
@Getter
@Setter
@Entity
@Table(name = "student_status_history")
public class StudentStatusHistory extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private Student.Status oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private Student.Status newStatus;

    /** Why, e.g. "Unpaid fees"; may be empty. */
    @Column(name = "reason")
    private String reason;

    /** The admin who changed it (users.id). */
    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;
}
