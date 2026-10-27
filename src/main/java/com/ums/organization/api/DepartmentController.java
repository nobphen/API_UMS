package com.ums.organization.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.organization.OrganizationPermissions;
import com.ums.organization.api.dto.DepartmentRequest;
import com.ums.organization.api.dto.DepartmentResponse;
import com.ums.organization.application.DepartmentService;
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
 * Departments of a faculty (Computer Science, Civil Engineering …).
 * Reading is open to every logged-in user (lists and dropdowns, e.g. choosing a department);
 * creating, changing and deleting need DEPARTMENT_MANAGE.
 */
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Adds a department to a faculty. The code is saved in capitals (cs → CS) and must not be used by another
     * department, deleted ones included.
     * <pre>POST /api/v1/departments  {"facultyId": 1, "code": "CS", "name": "Computer Science", "nameKh": "…"}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(OrganizationPermissions.DEPARTMENT_MANAGE)
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.created(departmentService.create(request));
    }

    /**
     * One page of departments, deleted ones left out; search matches the code, English or Khmer name,
     * facultyId keeps only that faculty's departments.
     * <pre>GET /api/v1/departments?facultyId=1&search=cs&page=1&size=20&sort=name,asc</pre>
     * Without page/size/sort: the first 20, ordered by name. Without facultyId: all faculties.
     */
    @GetMapping
    public ApiResponse<PageResponse<DepartmentResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long facultyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.page(departmentService.list(search, facultyId, pageable));
    }

    /**
     * One department; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/departments/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.get(id));
    }

    /**
     * Replaces all the department's fields (send every field, like for create). Changing facultyId moves it
     * to another faculty.
     * <pre>PUT /api/v1/departments/1  {"facultyId": 1, "code": "CS", "name": "Computer Science and AI", …}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissions.DEPARTMENT_MANAGE)
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.updated(departmentService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the department is no longer listed.
     * Refused (400) while programs still belong to it.
     * <pre>DELETE /api/v1/departments/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissions.DEPARTMENT_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.deleted();
    }
}
