package com.ums.exam.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A student asks for a final grade to be reviewed. */
@Getter
@Setter
@Entity
@Table(name = "grade_appeals")
public class GradeAppeal extends BaseEntity {

    /** The appealed grade (enrollment module). */
    @Column(name = "final_grade_id", nullable = false)
    private Long finalGradeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    /** Total score before the review. */
    @Column(name = "old_score")
    private BigDecimal oldScore;

    /** Total score after an accepted review. */
    @Column(name = "new_score")
    private BigDecimal newScore;

    /** The staff member who reviewed the appeal. */
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    public enum Status { PENDING, REVIEWING, ACCEPTED, REJECTED }
    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
