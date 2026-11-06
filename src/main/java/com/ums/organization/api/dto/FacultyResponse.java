package com.ums.organization.api.dto;

/** A faculty as sent back by the API. */
public record FacultyResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        Long deanStaffId) {
}
