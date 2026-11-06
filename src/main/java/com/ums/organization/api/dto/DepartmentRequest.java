package com.ums.organization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body to create or update a department. */
public record DepartmentRequest(
        @NotNull Long facultyId,                   // the faculty it belongs to
        @NotBlank @Size(max = 20) String code,     // unique
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String nameKh,            // name in Khmer, optional
        Long headStaffId) {                        // head of department (staff id), optional
}
