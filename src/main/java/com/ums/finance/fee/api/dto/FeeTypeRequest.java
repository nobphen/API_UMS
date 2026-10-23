package com.ums.finance.fee.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeeTypeRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        boolean refundable) {   // empty = false
}
