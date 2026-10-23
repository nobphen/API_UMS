package com.ums.finance.fee.api.dto;

import com.ums.finance.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record FeeStructureRequest(
        @NotNull Long programId,
        @NotNull Long academicYearId,
        @NotNull Long feeTypeId,
        @Positive Integer yearLevel,   // empty = 1
        @NotNull @PositiveOrZero BigDecimal amount,
        Currency currency) {   // empty = USD
}
