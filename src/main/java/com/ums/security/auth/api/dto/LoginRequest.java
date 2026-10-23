package com.ums.security.auth.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank String username,   // username or email
        @NotBlank String password,
        @NotNull @Valid DeviceInfo device,
        Boolean rememberMe) {
}
