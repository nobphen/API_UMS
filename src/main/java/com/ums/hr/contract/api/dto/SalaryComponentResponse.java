package com.ums.hr.contract.api.dto;

import com.ums.hr.contract.domain.SalaryComponent;

public record SalaryComponentResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        SalaryComponent.ComponentType componentType,
        boolean taxable,
        SalaryComponent.CalcType calcType) {
}
