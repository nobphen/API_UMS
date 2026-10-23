package com.ums.organization.api.dto;

import com.ums.organization.domain.Program;
import java.math.BigDecimal;

public record ProgramResponse(
        Long id,
        Long departmentId,
        String code,
        String name,
        String nameKh,
        Program.DegreeLevel degreeLevel,
        BigDecimal durationYears,
        int totalCredits,
        String status) {
}
