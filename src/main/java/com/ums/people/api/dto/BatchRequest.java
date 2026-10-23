package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BatchRequest(
        @NotNull Long programId,
        @NotBlank @Size(max = 50) String name,
        @NotNull Integer startYear) {
}
