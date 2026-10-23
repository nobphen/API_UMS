package com.ums.hr.payroll.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Hours a lecturer teaches in one class section, used to pay part-time and visiting lecturers. */
@Getter
@Setter
@Entity
@Table(name = "teaching_loads")
public class TeachingLoad extends BaseEntity {

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "hours_planned", nullable = false)
    private BigDecimal hoursPlanned = BigDecimal.ZERO;

    @Column(name = "hours_taught", nullable = false)
    private BigDecimal hoursTaught = BigDecimal.ZERO;

    /** Pay per hour; empty = the hourly rate of the contract. */
    @Column(name = "rate")
    private BigDecimal rate;

    /** Hours taught x rate, worked out by payroll. */
    @Column(name = "amount")
    private BigDecimal amount;
}
