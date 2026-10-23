package com.ums.enrollment;

/** Permission codes for the ENROLLMENT module. Must match V2__seed_permissions.sql. */
public final class EnrollmentPermissions {

    public static final String ENROLLMENT_MANAGE = "ENROLLMENT_MANAGE";
    public static final String ENROLLMENT_SELF = "ENROLLMENT_SELF";
    public static final String ATTENDANCE_TAKE = "ATTENDANCE_TAKE";
    public static final String ATTENDANCE_VIEW = "ATTENDANCE_VIEW";
    public static final String SCORE_ENTER = "SCORE_ENTER";
    public static final String GRADE_SUBMIT = "GRADE_SUBMIT";
    public static final String GRADE_APPROVE = "GRADE_APPROVE";
    public static final String GRADE_PUBLISH = "GRADE_PUBLISH";
    public static final String GRADE_VIEW_OWN = "GRADE_VIEW_OWN";

    private EnrollmentPermissions() {
    }
}
