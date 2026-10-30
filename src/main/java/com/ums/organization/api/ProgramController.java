package com.ums.organization.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.organization.OrganizationPermissions;
import com.ums.organization.api.dto.ProgramRequest;
import com.ums.organization.api.dto.ProgramResponse;
import com.ums.organization.application.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Degree programs of a department (Bachelor of Computer Science, Master of IT …).
 * Reading is open to every logged-in user (lists and dropdowns, e.g. choosing a program);
 * creating, changing and deleting need PROGRAM_MANAGE.
 */
@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    /**
     * Adds a program to a department. The code is saved in capitals (bcs → BCS) and must not be used by another
     * program, deleted ones included. Empty status → ACTIVE.
     * <pre>POST /api/v1/programs  {"departmentId": 1, "code": "BCS", "name": "Bachelor of Computer Science",
     *       "nameKh": "…", "degreeLevel": "BACHELOR", "durationYears": 4.0, "totalCredits": 120}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(OrganizationPermissions.PROGRAM_MANAGE)
    public ApiResponse<ProgramResponse> create(@Valid @RequestBody ProgramRequest request) {
        return ApiResponse.created(programService.create(request));
    }

    /**
     * One page of programs, deleted ones left out; search matches the code, English or Khmer name,
     * departmentId keeps only that department's programs.
     * <pre>GET /api/v1/programs?departmentId=1&search=bcs&page=1&size=20&sort=name,asc</pre>
     * Without page/size/sort: the first 20, ordered by name. Without departmentId: all departments.
     */
    @GetMapping
    public ApiResponse<PageResponse<ProgramResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.page(programService.list(search, departmentId, pageable));
    }

    /**
     * One program; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/programs/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<ProgramResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(programService.get(id));
    }

    /**
     * Replaces all the program's fields (send every field, like for create). Changing departmentId moves it
     * to another department.
     * <pre>PUT /api/v1/programs/1  {"departmentId": 1, "code": "BCS", "name": "Bachelor of Computer Science", …}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissions.PROGRAM_MANAGE)
    public ApiResponse<ProgramResponse> update(@PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        return ApiResponse.updated(programService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the program is no longer listed.
     * <pre>DELETE /api/v1/programs/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissions.PROGRAM_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ApiResponse.deleted();
    }
}
