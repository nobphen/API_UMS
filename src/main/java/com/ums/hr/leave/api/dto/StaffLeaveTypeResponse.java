package com.ums.hr.leave.api.dto;

import java.math.BigDecimal;

public record StaffLeaveTypeResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        BigDecimal daysPerYear,
        boolean paid) {
}
