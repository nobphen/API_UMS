package com.ums.hr.payroll.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record TeachingLoadRequest(
        @NotNull Long staffId,
        @NotNull Long classSectionId,
        @NotNull Long semesterId,
        @PositiveOrZero BigDecimal hoursPlanned,   // empty = 0
        @PositiveOrZero BigDecimal hoursTaught,    // empty = 0
        @PositiveOrZero BigDecimal rate) {         // empty = the contract's hourly rate
}
