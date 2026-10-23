package com.ums.people.api.dto;

import com.ums.people.domain.GeoArea;

public record GeoAreaResponse(
        Long id,
        String code,
        GeoArea.Level level,
        Long parentId,
        String nameKh,
        String nameEn,
        String unitKh,
        String unitEn,
        boolean active) {
}
