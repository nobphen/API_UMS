package com.ums.finance.fee.api.dto;

import com.ums.finance.Currency;
import java.math.BigDecimal;

public record FeeStructureResponse(
        Long id,
        Long programId,
        Long academicYearId,
        Long feeTypeId,
        int yearLevel,
        BigDecimal amount,
        Currency currency) {
}
