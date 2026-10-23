package com.ums.academic.course.api.dto;

public record CourseResponse(
        Long id,
        Long departmentId,
        String code,
        String title,
        String titleKh,
        int credits,
        int theoryHours,
        int labHours,
        String description,
        String descriptionKh) {
}
