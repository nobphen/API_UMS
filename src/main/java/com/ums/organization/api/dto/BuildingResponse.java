package com.ums.organization.api.dto;

/** A building as sent back by the API. */
public record BuildingResponse(
        Long id,
        String code,
        String name,
        String nameKh) {
}
