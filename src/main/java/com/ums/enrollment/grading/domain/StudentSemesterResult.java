package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** A student's totals for one semester: credits, GPA, cumulative GPA and academic standing. */
@Getter
@Setter
@Entity
@Table(name = "student_semester_results")
public class StudentSemesterResult extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "credits_attempted", nullable = false)
    private int creditsAttempted = 0;

    @Column(name = "credits_earned", nullable = false)
    private int creditsEarned = 0;

    @Column(name = "gpa")
    private BigDecimal gpa;

    /** Cumulative GPA up to and including this semester. */
    @Column(name = "cgpa")
    private BigDecimal cgpa;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_standing")
    private AcademicStanding academicStanding;

    public enum AcademicStanding { GOOD, PROBATION, DISMISSED }
}
