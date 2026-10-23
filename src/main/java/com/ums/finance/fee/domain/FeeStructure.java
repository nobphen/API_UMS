package com.ums.finance.fee.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.finance.Currency;
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

/** The price of one fee type for a program, academic year and year of study. */
@Getter
@Setter
@Entity
@Table(name = "fee_structures")
public class FeeStructure extends BaseEntity {

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_type_id")
    private FeeType feeType;

    @Column(name = "year_level", nullable = false)
    private int yearLevel = 1;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;
}
