package com.ums.security.role.api.dto;

/** One permission, as shown on the "Edit role" screen (grouped by module). */
public record PermissionResponse(Long id, String code, String module, String action, String description) {
}
