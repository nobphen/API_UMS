package com.ums.academic.course.api.dto;

public record CurriculumResponse(
        Long id,
        Long programId,
        String version,
        int effectiveYear,
        String status) {
}
