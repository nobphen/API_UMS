package com.ums.admission.intake.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** An admission round (e.g. "2026 main intake") during which applications are accepted. */
@Getter
@Setter
@Entity
@Table(name = "admission_intakes")
public class AdmissionIntake extends BaseEntity {

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    public enum Status { DRAFT, OPEN, CLOSED }
}
