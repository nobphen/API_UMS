package com.ums.security.menu.api.dto;

public record MenuResponse(
        Long id,
        Long parentId,
        String name,
        String nameKh,
        String path,
        String icon,
        int sortOrder,
        Long permissionId) {
}
