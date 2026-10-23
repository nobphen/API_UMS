package com.ums.lms.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One go of a student at an online quiz. */
@Getter
@Setter
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt extends BaseEntity {

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    /** The student's answers per question, as JSON. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers")
    private String answers;

    @Column(name = "score")
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.IN_PROGRESS;

    public enum Status { IN_PROGRESS, SUBMITTED, GRADED }
}
