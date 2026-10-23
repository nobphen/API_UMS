package com.ums.asset.api.dto;

import com.ums.asset.domain.Asset;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        String assetCode,
        String name,
        Long categoryId,
        String serialNo,
        Long roomId,
        Long departmentId,
        LocalDate purchaseDate,
        BigDecimal purchaseCost,
        String supplier,
        LocalDate warrantyUntil,
        Asset.Status status) {
}
