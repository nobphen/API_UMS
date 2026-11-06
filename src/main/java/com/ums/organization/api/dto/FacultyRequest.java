package com.ums.organization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body to create or update a faculty. */
public record FacultyRequest(
        @NotBlank @Size(max = 20) String code,     // unique
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String nameKh,            // name in Khmer, optional
        Long deanStaffId) {                        // the dean (staff id), optional
}
