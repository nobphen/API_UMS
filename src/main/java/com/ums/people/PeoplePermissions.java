package com.ums.people;

/** Permission codes for the PEOPLE module. Must match V2__seed_permissions.sql. */
public final class PeoplePermissions {

    public static final String STUDENT_VIEW = "STUDENT_VIEW";
    public static final String STUDENT_CREATE = "STUDENT_CREATE";
    public static final String STUDENT_UPDATE = "STUDENT_UPDATE";
    public static final String STAFF_VIEW = "STAFF_VIEW";
    public static final String STAFF_MANAGE = "STAFF_MANAGE";
    public static final String GUARDIAN_MANAGE = "GUARDIAN_MANAGE";
    public static final String GEO_AREA_MANAGE = "GEO_AREA_MANAGE";

    private PeoplePermissions() {
    }
}
