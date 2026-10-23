package com.ums.support.api.dto;

import java.time.OffsetDateTime;

public record SystemSettingResponse(
        String key,
        String value,
        String description,
        Long updatedBy,
        OffsetDateTime updatedAt) {
}
