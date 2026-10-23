package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.enrollment.registration.domain.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** The course result of one enrollment, computed from its assessment scores. */
@Getter
@Setter
@Entity
@Table(name = "final_grades")
public class FinalGrade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", unique = true)
    private Enrollment enrollment;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "letter_grade")
    private String letterGrade;

    @Column(name = "grade_point")
    private BigDecimal gradePoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    /** The staff member who approved the grade. */
    @Column(name = "approved_by")
    private Long approvedBy;

    /** When students could first see the grade. */
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    public enum Status { DRAFT, SUBMITTED, APPROVED, PUBLISHED }
}
