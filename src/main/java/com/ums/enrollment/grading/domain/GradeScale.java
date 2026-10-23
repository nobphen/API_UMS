package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Maps a range of total scores to a letter grade and grade point. */
@Getter
@Setter
@Entity
@Table(name = "grade_scales")
public class GradeScale extends BaseEntity {

    @Column(name = "letter", nullable = false, unique = true)
    private String letter;

    @Column(name = "min_score", nullable = false)
    private BigDecimal minScore;

    @Column(name = "max_score", nullable = false)
    private BigDecimal maxScore;

    @Column(name = "grade_point", nullable = false)
    private BigDecimal gradePoint;

    /** The letter earns the course credits. */
    @Column(name = "is_pass", nullable = false)
    private boolean pass = true;
}
