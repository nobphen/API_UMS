package com.ums.hr.leave.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "staff_leave_types")
public class StaffLeaveType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    /** Yearly entitlement; null = no limit. */
    @Column(name = "days_per_year")
    private BigDecimal daysPerYear;

    @Column(name = "is_paid", nullable = false)
    private boolean paid = true;
}
