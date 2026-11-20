package com.ums.academic.calendar.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** One school year (table academic_years), e.g. 2026-2027. Its semesters point to it. Only one is current. */
@Getter
@Setter
@Auditable
@Entity
@Table(name = "academic_years")
public class AcademicYear extends SoftDeletableEntity {

    /** e.g. 2026-2027. */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** First day of the year. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Last day of the year (after startDate). */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** The year now running. */
    @Column(name = "is_current", nullable = false)
    private boolean current;
}
