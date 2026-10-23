package com.ums.people.api.dto;

public record BatchResponse(
        Long id,
        Long programId,
        String name,
        String nameKh,
        int startYear) {
}
