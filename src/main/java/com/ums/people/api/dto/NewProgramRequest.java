package com.ums.people.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * An existing student starts another program: Bachelor → Master → PhD, or a second degree at the same time.
 * The person and the login are reused; only a new student record (new code, new program) is made.
 */
public record NewProgramRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{validation.username_chars}")
        String studentCode,   // e.g. MS2026014; the login keeps its old username

        @NotNull Long programId,
        Long batchId,
        @NotNull LocalDate admissionDate,
        @Min(1) @Max(10) Integer yearLevel,   // empty = 1; higher for credit transfer
        Integer expectedGraduationYear) {
}
