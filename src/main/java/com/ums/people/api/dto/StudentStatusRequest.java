package com.ums.people.api.dto;

import com.ums.people.domain.Student;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A real event in the student's life: suspended, on leave, back to ACTIVE, graduated or dropped. */
public record StudentStatusRequest(
        @NotNull Student.Status status,
        @Size(max = 255) String reason) {   // e.g. "Unpaid fees"
}
