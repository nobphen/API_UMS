package com.ums.graduation;

/** Permission codes for the GRADUATION module. Must match V2__seed_permissions.sql. */
public final class GraduationPermissions {

    public static final String GRADUATION_VERIFY = "GRADUATION_VERIFY";
    public static final String GRADUATION_APPROVE = "GRADUATION_APPROVE";
    public static final String TRANSCRIPT_ISSUE = "TRANSCRIPT_ISSUE";
    public static final String CERTIFICATE_ISSUE = "CERTIFICATE_ISSUE";
    public static final String THESIS_MANAGE = "THESIS_MANAGE";
    public static final String THESIS_SUPERVISE = "THESIS_SUPERVISE";
    public static final String THESIS_SUBMIT = "THESIS_SUBMIT";

    private GraduationPermissions() {
    }
}
