package com.ums.hr.leave.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StaffLeaveTypeRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @PositiveOrZero BigDecimal daysPerYear,   // empty = no limit
        Boolean paid) {   // empty = true
}
