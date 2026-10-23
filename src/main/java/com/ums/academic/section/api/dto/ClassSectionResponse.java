package com.ums.academic.section.api.dto;

import com.ums.academic.section.domain.ClassSection;

public record ClassSectionResponse(
        Long id,
        Long courseId,
        Long semesterId,
        String sectionCode,
        int capacity,
        ClassSection.Status status) {
}
