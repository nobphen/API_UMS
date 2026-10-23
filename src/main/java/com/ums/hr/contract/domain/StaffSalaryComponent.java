package com.ums.hr.contract.domain;

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

/** An allowance or deduction a staff member gets in every payroll period while it is valid. */
@Getter
@Setter
@Entity
@Table(name = "staff_salary_components")
public class StaffSalaryComponent extends BaseEntity {

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id")
    private SalaryComponent component;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /** Empty = no end. */
    @Column(name = "valid_to")
    private LocalDate validTo;
}
