package com.ums.security.auth.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Body of POST /auth/login: login name + password, the device, and rememberMe (saved on the session; empty = true). */
public record LoginRequest(
        @NotBlank String username,   // username, email or phone number
        @NotBlank String password,
        @NotNull @Valid DeviceInfo device,
        Boolean rememberMe) {
}
