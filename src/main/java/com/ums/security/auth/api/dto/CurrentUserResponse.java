package com.ums.security.auth.api.dto;

import java.util.List;

/** Who the access token belongs to, read from its claims. */
public record CurrentUserResponse(Long id, String username, String userType, Long sessionId, List<String> authorities,
                                  boolean mustChangePassword) {
}
