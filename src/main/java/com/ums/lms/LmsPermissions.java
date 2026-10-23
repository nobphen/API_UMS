package com.ums.lms;

/** Permission codes for the LMS module. Must match V2__seed_permissions.sql. */
public final class LmsPermissions {

    public static final String MATERIAL_MANAGE = "MATERIAL_MANAGE";
    public static final String ASSIGNMENT_SUBMIT = "ASSIGNMENT_SUBMIT";
    public static final String QUIZ_MANAGE = "QUIZ_MANAGE";
    public static final String QUIZ_TAKE = "QUIZ_TAKE";

    private LmsPermissions() {
    }
}
