package com.ums.organization.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.organization.OrganizationPermissions;
import com.ums.organization.api.dto.FacultyRequest;
import com.ums.organization.api.dto.FacultyResponse;
import com.ums.organization.application.FacultyService;
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
 * Faculties of the university (Engineering, IT, Business …).
 * Reading is open to every logged-in user (lists and dropdowns, e.g. choosing a faculty);
 * creating, changing and deleting need FACULTY_MANAGE.
 */
@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    /**
     * Adds a faculty. The code is saved in capitals (eng → ENG) and must not be used by another faculty,
     * deleted ones included.
     * <pre>POST /api/v1/faculties  {"code": "ENG", "name": "Faculty of Engineering", "nameKh": "…"}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(OrganizationPermissions.FACULTY_MANAGE)
    public ApiResponse<FacultyResponse> create(@Valid @RequestBody FacultyRequest request) {
        return ApiResponse.created(facultyService.create(request));
    }

    /**
     * One page of faculties, deleted ones left out; search matches the code, English or Khmer name.
     * <pre>GET /api/v1/faculties?search=eng&page=1&size=20&sort=name,asc</pre>
     * Without page/size/sort: the first 20, ordered by name.
     */
    @GetMapping
    public ApiResponse<PageResponse<FacultyResponse>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20,
            sort = "name") Pageable pageable
    ) {
        return ApiResponse.page(facultyService.list(search, pageable));
    }

    /**
     * One faculty; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/faculties/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<FacultyResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(facultyService.get(id));
    }

    /**
     * Replaces all the faculty's fields (send every field, like for create).
     * <pre>PUT /api/v1/faculties/1  {"code": "ENG", "name": "Faculty of Engineering and Technology", …}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissions.FACULTY_MANAGE)
    public ApiResponse<FacultyResponse> update(@PathVariable Long id, @Valid @RequestBody FacultyRequest request) {
        return ApiResponse.updated(facultyService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the faculty is no longer listed.
     * Refused (400) while departments still belong to it.
     * <pre>DELETE /api/v1/faculties/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissions.FACULTY_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ApiResponse.deleted();
    }
}
