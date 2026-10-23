package com.ums.security.menu.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuRequest(
        Long parentId,   // empty = top-level item
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        @Size(max = 200) String path,
        @Size(max = 50) String icon,
        Integer sortOrder,   // empty = 0
        Long permissionId) {
}
