package com.ums.security;

/** Permission codes for the SECURITY module. Must match V2__seed_permissions.sql. */
public final class SecurityPermissions {

    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_LOCK = "USER_LOCK";
    public static final String ROLE_VIEW = "ROLE_VIEW";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";
    public static final String AUDIT_VIEW = "AUDIT_VIEW";

    private SecurityPermissions() {
    }
}
