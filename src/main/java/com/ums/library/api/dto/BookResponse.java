package com.ums.library.api.dto;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String authors,
        String publisher,
        Integer publishYear,
        String edition,
        String language,
        Long categoryId,
        String coverUrl) {
}
