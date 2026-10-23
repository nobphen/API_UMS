package com.ums.finance.scholarship.api.dto;

public record StudentScholarshipResponse(
        Long id,
        Long studentId,
        Long scholarshipId,
        Long academicYearId,
        Long approvedBy,
        String status) {
}
