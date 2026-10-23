package com.ums.finance;

/** Permission codes for the FINANCE module. Must match V2__seed_permissions.sql. */
public final class FinancePermissions {

    public static final String FEE_MANAGE = "FEE_MANAGE";
    public static final String INVOICE_VIEW = "INVOICE_VIEW";
    public static final String INVOICE_CREATE = "INVOICE_CREATE";
    public static final String PAYMENT_RECEIVE = "PAYMENT_RECEIVE";
    public static final String PAYMENT_REVERSE = "PAYMENT_REVERSE";
    public static final String SCHOLARSHIP_MANAGE = "SCHOLARSHIP_MANAGE";
    public static final String FINANCE_REPORT_VIEW = "FINANCE_REPORT_VIEW";

    private FinancePermissions() {
    }
}
