package com.ums.exam;

/** Permission codes for the EXAM module. Must match V2__seed_permissions.sql. */
public final class ExamPermissions {

    public static final String EXAM_SCHEDULE_MANAGE = "EXAM_SCHEDULE_MANAGE";
    public static final String EXAM_SEAT_MANAGE = "EXAM_SEAT_MANAGE";
    public static final String GRADE_APPEAL_CREATE = "GRADE_APPEAL_CREATE";
    public static final String GRADE_APPEAL_REVIEW = "GRADE_APPEAL_REVIEW";

    private ExamPermissions() {
    }
}
