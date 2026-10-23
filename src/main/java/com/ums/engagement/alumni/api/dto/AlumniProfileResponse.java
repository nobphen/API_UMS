package com.ums.engagement.alumni.api.dto;

public record AlumniProfileResponse(
        Long id,
        Long studentId,
        int graduationYear,
        String currentEmployer,
        String jobTitle,
        String industry,
        String linkedinUrl,
        boolean consentToContact) {
}
