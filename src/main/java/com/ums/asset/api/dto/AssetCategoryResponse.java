package com.ums.asset.api.dto;

public record AssetCategoryResponse(
        Long id,
        String code,
        String name,
        Integer depreciationYears) {
}
