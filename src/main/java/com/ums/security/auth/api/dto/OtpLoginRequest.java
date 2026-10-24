package com.ums.security.auth.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Step 2 of the SMS code login: the phone number again and the code from the SMS. */
public record OtpLoginRequest(
        @NotBlank @Size(max = 30) String phone,   // same value as sent to /otp/send
        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "{validation.reset_code}")
        String code,
        @NotNull @Valid DeviceInfo device,
        Boolean rememberMe) {
}
