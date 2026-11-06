package com.ums.organization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body to create or update a building. */
public record BuildingRequest(
        @NotBlank @Size(max = 20) String code,     // unique
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh) {          // name in Khmer, optional
}
