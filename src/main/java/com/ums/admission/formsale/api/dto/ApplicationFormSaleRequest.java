package com.ums.admission.formsale.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** A form sold at the counter. The form number is generated. */
public record ApplicationFormSaleRequest(
        @NotNull Long intakeId,
        @NotBlank @Size(max = 150) String buyerName,
        @Size(max = 30) String buyerPhone,
        @NotNull @PositiveOrZero BigDecimal amount) {
}
