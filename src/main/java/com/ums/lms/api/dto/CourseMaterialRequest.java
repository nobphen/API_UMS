package com.ums.lms.api.dto;

import com.ums.lms.domain.CourseMaterial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CourseMaterialRequest(
        @NotNull Long classSectionId,
        @Positive Integer weekNo,
        @NotBlank @Size(max = 200) String title,
        @NotNull CourseMaterial.MaterialType materialType,
        @Size(max = 500) String fileUrl,
        @Size(max = 500) String linkUrl,
        String content,
        OffsetDateTime publishedAt) {   // students see it from then; empty = draft
}
