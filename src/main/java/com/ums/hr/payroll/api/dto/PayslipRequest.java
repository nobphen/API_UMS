package com.ums.hr.payroll.api.dto;

import jakarta.validation.constraints.NotNull;

/** Adds a payslip for one staff member to a payroll period; the amounts are worked out by payroll. */
public record PayslipRequest(
        @NotNull Long payrollPeriodId,
        @NotNull Long staffId,
        Long contractId) {   // empty = the staff member's current contract
}
