package com.ums.finance.scholarship.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A scholarship granted to a student for one academic year. */
@Getter
@Setter
@Entity
@Table(name = "student_scholarships")
public class StudentScholarship extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scholarship_id")
    private Scholarship scholarship;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    /** The staff member who approved it. */
    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
