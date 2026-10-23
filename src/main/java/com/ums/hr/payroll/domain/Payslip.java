package com.ums.hr.payroll.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.hr.contract.domain.EmploymentContract;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** One staff member's pay for one payroll period; the totals are summed from its items. */
@Getter
@Setter
@Entity
@Table(name = "payslips")
public class Payslip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_period_id")
    private PayrollPeriod payrollPeriod;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private EmploymentContract contract;

    @Column(name = "gross", nullable = false)
    private BigDecimal gross = BigDecimal.ZERO;

    @Column(name = "total_allowance", nullable = false)
    private BigDecimal totalAllowance = BigDecimal.ZERO;

    @Column(name = "total_deduction", nullable = false)
    private BigDecimal totalDeduction = BigDecimal.ZERO;

    @Column(name = "tax", nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "net", nullable = false)
    private BigDecimal net = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    public enum Status { DRAFT, APPROVED, PAID }
}
