package com.ums.hr.payroll.api.dto;

import com.ums.hr.payroll.domain.Payslip;
import java.math.BigDecimal;

public record PayslipResponse(
        Long id,
        Long payrollPeriodId,
        Long staffId,
        Long contractId,
        BigDecimal gross,
        BigDecimal totalAllowance,
        BigDecimal totalDeduction,
        BigDecimal tax,
        BigDecimal net,
        Payslip.Status status) {
}
