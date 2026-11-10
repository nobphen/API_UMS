package com.ums.people.api.dto;

/** A batch sent back to the client by BatchController (create, get, list, update). */
public record BatchResponse(
        Long id,
        Long programId,
        String name,
        String nameKh,
        int startYear) {
}
