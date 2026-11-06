package com.ums.organization.api.dto;

/** A department as sent back by the API. */
public record DepartmentResponse(
        Long id,
        Long facultyId,
        String code,
        String name,
        String nameKh,
        Long headStaffId) {
}
