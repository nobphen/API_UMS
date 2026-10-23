package com.ums.hr.contract.api.dto;

import com.ums.hr.contract.domain.EmploymentContract;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmploymentContractResponse(
        Long id,
        String contractNo,
        Long staffId,
        EmploymentContract.ContractType contractType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseSalary,
        BigDecimal hourlyRate,
        EmploymentContract.Currency currency,
        EmploymentContract.Status status) {
}
