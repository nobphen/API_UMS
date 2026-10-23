package com.ums.request;

/** Permission codes for the REQUEST module. Must match V2__seed_permissions.sql. */
public final class RequestPermissions {

    public static final String LEAVE_REQUEST_CREATE = "LEAVE_REQUEST_CREATE";
    public static final String LEAVE_REQUEST_APPROVE = "LEAVE_REQUEST_APPROVE";
    public static final String LEAVE_REQUEST_VIEW_ALL = "LEAVE_REQUEST_VIEW_ALL";
    public static final String STUDENT_REQUEST_CREATE = "STUDENT_REQUEST_CREATE";
    public static final String STUDENT_REQUEST_APPROVE = "STUDENT_REQUEST_APPROVE";

    private RequestPermissions() {
    }
}
