package com.ums.support.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentRequest(
        @NotBlank @Size(max = 30) String ownerType,
        @NotNull Long ownerId,
        @Size(max = 30) String docType,
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 500) String fileUrl,
        @Size(max = 100) String mimeType) {
}
