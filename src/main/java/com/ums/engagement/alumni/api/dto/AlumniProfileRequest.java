package com.ums.engagement.alumni.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlumniProfileRequest(
        @NotNull Long studentId,
        @NotNull Integer graduationYear,
        @Size(max = 150) String currentEmployer,
        @Size(max = 100) String jobTitle,
        @Size(max = 100) String industry,
        @Size(max = 300) String linkedinUrl,
        boolean consentToContact) {
}
