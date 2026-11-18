package com.ums.security.audit.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.security.SecurityPermissions;
import com.ums.security.audit.api.dto.AuditLogRequest;
import com.ums.security.audit.api.dto.AuditLogResponse;
import com.ums.security.audit.application.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets admins search the audit log (who changed what, and when). Only for AUDIT_VIEW (ADMIN, SUPER_ADMIN). */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * One page of audit rows, newest first. Every filter is optional (empty = all); from / to are days, both included.
     * entity is the Java class name: Person, Student, Staff, Guardian, StudentGuardian ... (case-sensitive).
     * <pre>GET /api/v1/audit-logs?entity=Person&amp;entityId=2&amp;action=UPDATE&amp;userId=1&amp;from=2026-11-01&amp;to=2026-11-18&amp;page=1&amp;size=20</pre>
     */
    @GetMapping
    @RequirePermission(SecurityPermissions.AUDIT_VIEW)
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            AuditLogRequest filter,
            @PageableDefault(sort = "createdAt",size = 20,direction = Sort.Direction.DESC) Pageable pageable
            ){
        return ApiResponse.page(auditLogService.list(filter,pageable));
    }
}
