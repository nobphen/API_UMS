package com.ums.people.api.dto;

/** A batch sent back to the client. Not used yet. */
public record BatchResponse(
        Long id,
        Long programId,
        String name,
        String nameKh,
        int startYear) {
}
