package com.ums.support.api.dto;

import java.time.OffsetDateTime;
public record DocumentResponse(
        Long id,
        String ownerType,
        Long ownerId,
        String docType,
        String fileName,
        String fileUrl,
        String mimeType,
        Long uploadedBy,
        OffsetDateTime createdAt) {
}
