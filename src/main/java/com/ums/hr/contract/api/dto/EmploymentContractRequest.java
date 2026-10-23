package com.ums.hr.contract.api.dto;

import com.ums.hr.contract.domain.EmploymentContract;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmploymentContractRequest(
        @NotBlank @Size(max = 30) String contractNo,
        @NotNull Long staffId,
        @NotNull EmploymentContract.ContractType contractType,
        @NotNull LocalDate startDate,
        LocalDate endDate,   // empty = open-ended
        @PositiveOrZero BigDecimal baseSalary,   // empty = 0
        @PositiveOrZero BigDecimal hourlyRate,
        EmploymentContract.Currency currency) {   // empty = USD
}
