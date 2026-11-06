package com.ums.security.auth.application;

import java.util.Set;

/** A user's authorities (ROLE_* codes + permission codes), cached in Redis. */
public interface PermissionCacheService {

    /**
     * From Redis, or from the database on a cache miss (used at /auth/refresh).
     *
     * @param userId the user
     * @return ROLE_* codes and permission codes of the user's active roles
     */
    Set<String> getAuthorities(Long userId);

    /**
     * Always reads the database and refreshes the cache (used at login).
     *
     * @param userId the user
     * @return ROLE_* codes and permission codes of the user's active roles
     */
    Set<String> reload(Long userId);

    /**
     * Call after changing a user's roles or a role's permissions; the change shows at the next token refresh.
     *
     * @param userId the user whose cached authorities are dropped
     */
    void evict(Long userId);
}
