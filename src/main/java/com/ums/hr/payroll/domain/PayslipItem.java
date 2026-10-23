package com.ums.hr.payroll.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.hr.contract.domain.SalaryComponent;
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

/** One line of a payslip. */
@Getter
@Setter
@Entity
@Table(name = "payslip_items")
public class PayslipItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_id")
    private Payslip payslip;

    /** Set for ALLOWANCE and DEDUCTION lines. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private SalaryComponent component;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    public enum ItemType { BASIC, TEACHING, ALLOWANCE, DEDUCTION, TAX }
}
