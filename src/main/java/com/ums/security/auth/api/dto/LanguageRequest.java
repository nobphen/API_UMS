package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LanguageRequest(
        @NotBlank
        @Pattern(regexp = "^(km|en)$", message = "must be km or en")
        String language) {
}
