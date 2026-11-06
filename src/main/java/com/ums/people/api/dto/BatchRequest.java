package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Data to add or change a batch (a group of students of one program who started in the same year). Not used yet. */
public record BatchRequest(
        @NotNull Long programId,
        @NotBlank @Size(max = 50) String name,
        @Size(max = 50) String nameKh,
        @NotNull Integer startYear) {
}
