package com.ums.academic.course.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotNull Long departmentId,
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 200) String titleKh,
        @NotNull @Positive Integer credits,
        @PositiveOrZero Integer theoryHours,   // empty = 0
        @PositiveOrZero Integer labHours,      // empty = 0
        String description,
        String descriptionKh) {
}
