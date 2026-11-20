package com.ums.common.security;

import java.util.Objects;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Reads who is logged in from the access token (before: Long.valueOf(jwt.getSubject()) was written in each controller).
 * <pre>
 * &#64;GetMapping("/me")
 * public ApiResponse&lt;StaffResponse&gt; me(&#64;AuthenticationPrincipal Jwt jwt) {
 *     return ApiResponse.ok(staffService.getMine(CurrentUser.userId(jwt)));
 * }
 * </pre>
 */
public final class CurrentUser {

    /** Only static methods. */
    private CurrentUser() {
    }

    /**
     * The login's user id. The token's "sub" (subject) is users.id, written as text ("1").
     * Comes from the token, never from the request, so nobody can act as another user.
     *
     * @param jwt the access token (@AuthenticationPrincipal Jwt jwt)
     * @return users.id of the logged-in user
     */
    public static Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
