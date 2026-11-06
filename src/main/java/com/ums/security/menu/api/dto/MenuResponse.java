package com.ums.security.menu.api.dto;

/** One navigation menu item as sent to the client; parentId is null for a top-level item. */
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
