package com.ums.hr;

/** Permission codes for the HR module. Must match V2__seed_permissions.sql. */
public final class HrPermissions {

    public static final String CONTRACT_MANAGE = "CONTRACT_MANAGE";
    public static final String SALARY_COMPONENT_MANAGE = "SALARY_COMPONENT_MANAGE";
    public static final String STAFF_LEAVE_CREATE = "STAFF_LEAVE_CREATE";
    public static final String STAFF_LEAVE_APPROVE = "STAFF_LEAVE_APPROVE";
    public static final String TEACHING_LOAD_MANAGE = "TEACHING_LOAD_MANAGE";
    public static final String PAYROLL_PROCESS = "PAYROLL_PROCESS";
    public static final String PAYROLL_APPROVE = "PAYROLL_APPROVE";
    public static final String PAYSLIP_VIEW_OWN = "PAYSLIP_VIEW_OWN";

    private HrPermissions() {
    }
}
