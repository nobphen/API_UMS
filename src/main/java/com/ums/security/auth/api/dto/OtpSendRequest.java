package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Step 1 of the SMS code login: the phone number the code is sent to. */
public record OtpSendRequest(
        @NotBlank @Size(max = 30) String phone) {   // as typed: 012 345 678, +85512345678…
}
