package com.ums.hr.payroll.api.dto;

import com.ums.hr.payroll.domain.PayrollPeriod;
import java.time.LocalDate;

public record PayrollPeriodResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        PayrollPeriod.Status status,
        Long approvedBy) {
}
