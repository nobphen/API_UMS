package com.ums.organization.api.dto;

import com.ums.organization.domain.Program;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Body to create or update a program. */
public record ProgramRequest(
        @NotNull Long departmentId,                // the department that offers it
        @NotBlank @Size(max = 20) String code,     // unique
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String nameKh,            // name in Khmer, optional
        @NotNull Program.DegreeLevel degreeLevel,
        @NotNull @Positive @Digits(integer = 2, fraction = 1) BigDecimal durationYears,   // e.g. 4.0 or 1.5
        @NotNull @Positive Integer totalCredits,
        @Size(max = 20) String status) {   // empty = ACTIVE
}
