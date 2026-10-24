package com.ums.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * The endpoint needs one of these permission codes, e.g. {@code @RequirePermission(PeoplePermissions.STUDENT_CREATE)}
 * or {@code @RequirePermission({PeoplePermissions.STUDENT_VIEW, PeoplePermissions.STAFF_VIEW})}.
 * Short for {@code @PreAuthorize("hasAnyAuthority(...)")}; the codes are Java constants, so a typo does not compile.
 * Users get the permissions from their roles (V2/V3 seeds, then the roles API). Without it a call answers 403.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAnyAuthority('{value}'.split(','))")   // the codes arrive as A or A,B: quote, then split
public @interface RequirePermission {

    /** Permission codes from the module's *Permissions class; having any one of them is enough. */
    String[] value();
}
