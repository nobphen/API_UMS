package com.ums.hr.contract.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employment_contracts")
public class EmploymentContract extends BaseEntity {

    @Column(name = "contract_no", nullable = false, unique = true)
    private String contractNo;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Empty = open-ended. */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "base_salary", nullable = false)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    /** Pay per teaching hour (part-time and visiting lecturers). */
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public enum ContractType { FULL_TIME, PART_TIME, VISITING }

    public enum Currency { USD, KHR }

    public enum Status { ACTIVE, EXPIRED, TERMINATED }
}
