package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.enrollment.registration.domain.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** One student's score on one assessment. */
@Getter
@Setter
@Entity
@Table(name = "assessment_scores")
public class AssessmentScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    /** Null until graded. */
    @Column(name = "score")
    private BigDecimal score;

    /** The staff member who entered the score. */
    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(name = "graded_at")
    private OffsetDateTime gradedAt;
}
