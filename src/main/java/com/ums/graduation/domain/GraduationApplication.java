package com.ums.graduation.domain;

import com.ums.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** A student's request to graduate in an academic year; one per student and year. */
@Getter
@Setter
@Entity
@Table(name = "graduation_applications")
public class GraduationApplication extends AuditableEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "credits_completed")
    private Integer creditsCompleted;

    @Column(name = "cgpa")
    private BigDecimal cgpa;

    /** Credits and required courses of the curriculum are done. */
    @Column(name = "requirements_met", nullable = false)
    private boolean requirementsMet = false;

    /** No unpaid invoices left. */
    @Column(name = "fees_cleared", nullable = false)
    private boolean feesCleared = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    /** The staff member who approved it. */
    @Column(name = "approved_by")
    private Long approvedBy;

    public enum Status { PENDING, VERIFIED, APPROVED, REJECTED }
}
