package com.ums.hr.payroll.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PayrollPeriodRequest(
        @NotBlank @Size(max = 50) String name,   // e.g. 2026-10
        @Size(max = 50) String nameKh,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
