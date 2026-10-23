package com.ums.admission.intake.domain;

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

/** A program offered in an intake, with its seats and prices. */
@Getter
@Setter
@Entity
@Table(name = "intake_programs")
public class IntakeProgram extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intake_id")
    private AdmissionIntake intake;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    /** Seats available; null = no limit. */
    @Column(name = "quota")
    private Integer quota;

    @Column(name = "form_price", nullable = false)
    private BigDecimal formPrice = BigDecimal.ZERO;

    @Column(name = "admission_fee", nullable = false)
    private BigDecimal admissionFee = BigDecimal.ZERO;

    @Column(name = "exam_date")
    private LocalDate examDate;
}
