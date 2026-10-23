package com.ums.finance.scholarship.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.finance.fee.domain.FeeType;
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

@Getter
@Setter
@Entity
@Table(name = "scholarships")
public class Scholarship extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    /** A percentage (PERCENT) or an amount of money (FIXED). */
    @Column(name = "value", nullable = false)
    private BigDecimal value;

    /** The fee type the discount applies to; null = the whole invoice. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applies_to_fee_type_id")
    private FeeType appliesToFeeType;

    public enum DiscountType { PERCENT, FIXED }
}
