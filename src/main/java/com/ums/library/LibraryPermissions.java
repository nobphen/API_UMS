package com.ums.library;

/** Permission codes for the LIBRARY module. Must match V2__seed_permissions.sql. */
public final class LibraryPermissions {

    public static final String BOOK_MANAGE = "BOOK_MANAGE";
    public static final String LOAN_ISSUE = "LOAN_ISSUE";
    public static final String LOAN_RETURN = "LOAN_RETURN";
    public static final String RESERVATION_CREATE = "RESERVATION_CREATE";
    public static final String FINE_WAIVE = "FINE_WAIVE";
    public static final String LIBRARY_POLICY_MANAGE = "LIBRARY_POLICY_MANAGE";

    private LibraryPermissions() {
    }
}
