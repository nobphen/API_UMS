package com.ums.admission;

/** Permission codes for the ADMISSION module. Must match V2__seed_permissions.sql. */
public final class AdmissionPermissions {

    public static final String FORM_SALE_CREATE = "FORM_SALE_CREATE";
    public static final String FORM_SALE_VOID = "FORM_SALE_VOID";
    public static final String APPLICATION_VIEW = "APPLICATION_VIEW";
    public static final String APPLICATION_REVIEW = "APPLICATION_REVIEW";
    public static final String APPLICATION_DECIDE = "APPLICATION_DECIDE";
    public static final String APPLICANT_ENROLL = "APPLICANT_ENROLL";

    private AdmissionPermissions() {
    }
}
