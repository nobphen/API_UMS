package com.ums.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put on an entity to keep a history of its changes in audit_logs: every insert, update and delete,
 * with who did it, from which IP, and the values before and after. See {@link AuditLogListener}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
}
