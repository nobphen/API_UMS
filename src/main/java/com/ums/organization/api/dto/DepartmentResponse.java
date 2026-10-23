package com.ums.organization.api.dto;

public record DepartmentResponse(
        Long id,
        Long facultyId,
        String code,
        String name,
        String nameKh,
        Long headStaffId) {
}
