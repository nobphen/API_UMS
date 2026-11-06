package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Body of PUT /auth/me/language: the user's app language, "km" (Khmer) or "en" (English). */
public record LanguageRequest(
        @NotBlank
        @Pattern(regexp = "^(km|en)$", message = "{validation.language}")
        String language) {
}
