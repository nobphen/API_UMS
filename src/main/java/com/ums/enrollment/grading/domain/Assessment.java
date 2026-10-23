package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** A graded piece of work in a class section. */
@Getter
@Setter
@Entity
@Table(name = "assessments")
public class Assessment extends BaseEntity {

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_type_id")
    private AssessmentType assessmentType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    /** Share of the final grade, 0 to 100. */
    @Column(name = "weight_percent", nullable = false)
    private BigDecimal weightPercent;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
