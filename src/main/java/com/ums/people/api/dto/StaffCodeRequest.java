package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Fixes a mistyped staff code; same rules as the code in StaffRequest (it is also the username). */
public record StaffCodeRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{validation.username_chars}")
        String staffCode) {   // the correct code, e.g. LT2026010
}
