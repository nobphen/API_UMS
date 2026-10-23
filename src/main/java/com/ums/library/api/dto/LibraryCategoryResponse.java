package com.ums.library.api.dto;

public record LibraryCategoryResponse(
        Long id,
        String code,
        String name,
        Long parentId) {
}
