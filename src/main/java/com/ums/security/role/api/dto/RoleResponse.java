package com.ums.security.role.api.dto;

import java.util.List;

/** One role as the admin screens show it: names, status, permission codes and how many users hold it. */
public record RoleResponse(
        Long id,
        String code,
        String name,
        String nameKh,
        String description,
        String descriptionKh,
        boolean system,             // made by the seed scripts: can't be deleted
        String status,
        List<String> permissions,   // permission codes, sorted
        long userCount) {           // how many role assignments use it
}
