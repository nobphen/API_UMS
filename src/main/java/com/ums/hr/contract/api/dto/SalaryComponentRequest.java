package com.ums.hr.contract.api.dto;

import com.ums.hr.contract.domain.SalaryComponent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalaryComponentRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 100) String name,
        @NotNull SalaryComponent.ComponentType componentType,
        Boolean taxable,   // empty = true
        SalaryComponent.CalcType calcType) {   // empty = FIXED
}
