package com.ums.finance.scholarship.api.dto;

import com.ums.finance.scholarship.domain.Scholarship;
import java.math.BigDecimal;

public record ScholarshipResponse(
        Long id,
        String code,
        String name,
        Scholarship.DiscountType discountType,
        BigDecimal value,
        Long appliesToFeeTypeId) {
}
