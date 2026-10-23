package com.ums.finance.scholarship.api.dto;

import com.ums.finance.scholarship.domain.Scholarship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ScholarshipRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull Scholarship.DiscountType discountType,
        @NotNull @PositiveOrZero BigDecimal value,   // a percentage (PERCENT) or an amount (FIXED)
        Long appliesToFeeTypeId) {   // empty = the whole invoice
}
