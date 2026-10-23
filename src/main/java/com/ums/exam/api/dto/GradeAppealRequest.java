package com.ums.exam.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A student appeals one of their final grades; the student is the logged-in user. */
public record GradeAppealRequest(
        @NotNull Long finalGradeId,
        @NotBlank String reason) {
}
