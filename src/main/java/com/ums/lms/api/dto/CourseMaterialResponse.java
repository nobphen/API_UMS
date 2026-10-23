package com.ums.lms.api.dto;

import com.ums.lms.domain.CourseMaterial;
import java.time.OffsetDateTime;

public record CourseMaterialResponse(
        Long id,
        Long classSectionId,
        Integer weekNo,
        String title,
        CourseMaterial.MaterialType materialType,
        String fileUrl,
        String linkUrl,
        String content,
        OffsetDateTime publishedAt,
        Long createdBy,
        OffsetDateTime createdAt) {
}
