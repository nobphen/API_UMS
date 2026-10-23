package com.ums.hr.payroll.api.dto;

import java.math.BigDecimal;

public record TeachingLoadResponse(
        Long id,
        Long staffId,
        Long classSectionId,
        Long semesterId,
        BigDecimal hoursPlanned,
        BigDecimal hoursTaught,
        BigDecimal rate,
        BigDecimal amount) {
}
