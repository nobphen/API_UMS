package com.ums.asset.api.dto;

import com.ums.asset.domain.Asset;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(
        @NotBlank @Size(max = 30) String assetCode,
        @NotBlank @Size(max = 200) String name,
        @NotNull Long categoryId,
        @Size(max = 100) String serialNo,
        Long roomId,
        Long departmentId,
        LocalDate purchaseDate,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal purchaseCost,
        @Size(max = 150) String supplier,
        LocalDate warrantyUntil,
        Asset.Status status) {   // empty = IN_USE
}
