package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Fixes a mistyped student code; same rules as the code in StudentRequest (it can also be the username). */
public record StudentCodeRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{validation.username_chars}")
        String studentCode) {   // the correct code, e.g. ST2026010
}
