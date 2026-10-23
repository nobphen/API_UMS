package com.ums.library.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @Size(max = 20) String isbn,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 300) String authors,
        @Size(max = 150) String publisher,
        Integer publishYear,
        @Size(max = 30) String edition,
        @Size(max = 20) String language,
        Long categoryId,
        @Size(max = 500) String coverUrl) {
}
