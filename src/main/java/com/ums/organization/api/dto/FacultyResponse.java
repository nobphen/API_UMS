package com.ums.organization.api.dto;

public record FacultyResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        Long deanStaffId) {
}
