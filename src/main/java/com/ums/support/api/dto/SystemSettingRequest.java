package com.ums.support.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemSettingRequest(
        @NotBlank @Size(max = 100) String key,
        String value,
        String description) {
}
