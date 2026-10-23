package com.ums.admission.formsale.api.dto;

import com.ums.admission.formsale.domain.ApplicationFormSale;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApplicationFormSaleResponse(
        Long id,
        String formNo,
        Long intakeId,
        String buyerName,
        String buyerPhone,
        BigDecimal amount,
        Long paymentId,
        Long soldBy,
        OffsetDateTime soldAt,
        ApplicationFormSale.Status status) {
}
