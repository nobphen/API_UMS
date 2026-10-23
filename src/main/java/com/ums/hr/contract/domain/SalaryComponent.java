package com.ums.hr.contract.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A kind of allowance or deduction that can appear on a payslip. */
@Getter
@Setter
@Entity
@Table(name = "salary_components")
public class SalaryComponent extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

    @Column(name = "is_taxable", nullable = false)
    private boolean taxable = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "calc_type", nullable = false)
    private CalcType calcType = CalcType.FIXED;

    public enum ComponentType { ALLOWANCE, DEDUCTION }

    /** FIXED: the amount as is. PERCENT: the amount is a percentage. */
    public enum CalcType { FIXED, PERCENT }
}
