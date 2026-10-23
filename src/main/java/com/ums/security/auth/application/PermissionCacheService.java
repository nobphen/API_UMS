package com.ums.security.auth.application;

import java.util.Set;

/** A user's authorities (ROLE_* codes + permission codes), cached in Redis. */
public interface PermissionCacheService {

    /** From Redis, or from the database on a cache miss. */
    Set<String> getAuthorities(Long userId);

    /** Always reads the database and refreshes the cache (used at login). */
    Set<String> reload(Long userId);

    /** Call after changing a user's roles or a role's permissions. */
    void evict(Long userId);
}
