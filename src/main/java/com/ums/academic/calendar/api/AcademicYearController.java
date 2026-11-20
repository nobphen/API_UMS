package com.ums.academic.calendar.api;

import com.ums.academic.AcademicPermissions;
import com.ums.academic.calendar.api.dto.AcademicYearRequest;
import com.ums.academic.calendar.api.dto.AcademicYearResponse;
import com.ums.academic.calendar.application.AcademicYearService;
import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Academic years (e.g. "2026-2027"): the school year that semesters belong to.
 * Reading is open to every logged-in user (dropdowns, student app, reports).
 * Creating, changing, making current and deleting need SEMESTER_MANAGE (there is no separate year permission).
 */
@RestController
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    /**
     * New year, not current yet. 201. Name free (deleted years count too), start before end,
     * no day shared with another year.
     * <pre>POST /api/v1/academic-years  {"name": "2026-2027", "startDate": "2026-10-01", "endDate": "2027-09-30"}</pre>
     */
    @PostMapping
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AcademicYearResponse> create(@Valid @RequestBody AcademicYearRequest request) {
        return ApiResponse.created(academicYearService.create(request));
    }

    /**
     * Years (not deleted), newest first. No paging: only one year is added each year.
     * notEnded=true keeps only the years not ended yet (forms like admission intake or new semester);
     * without it, all years (reports, history). "Today" is decided by the server, not sent by the client.
     * <pre>
     * GET /api/v1/academic-years                  → all years
     * GET /api/v1/academic-years?notEnded=true    → only years with endDate >= today
     * </pre>
     */
    @GetMapping
    public ApiResponse<List<AcademicYearResponse>> list(@RequestParam(defaultValue = "false") boolean notEnded) {
        return ApiResponse.ok(academicYearService.list(notEnded));
    }

    /**
     * The current year; 404 when no year is current yet.
     * <pre>GET /api/v1/academic-years/current</pre>
     */
    @GetMapping("/current")
    public ApiResponse<AcademicYearResponse> getCurrent() {
        return ApiResponse.ok(academicYearService.getCurrent());
    }

    /**
     * One year; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/academic-years/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<AcademicYearResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(academicYearService.get(id));
    }

    /**
     * Replaces name and dates (send every field, like for create). Its semesters must stay inside
     * the new dates (400). "current" is not changed here.
     * Use the id of the year whose name you send (e.g. id 2 = 2026-2027), or another year's name → 400 name_exists.
     * <pre>PUT /api/v1/academic-years/2  {"name": "2026-2027", "startDate": "2026-10-01", "endDate": "2027-08-31"}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<AcademicYearResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody AcademicYearRequest request) {
        return ApiResponse.updated(academicYearService.update(id, request));
    }

    /**
     * Makes this year the current one; the old current year is switched off. No body.
     * PUT, not GET: it changes data, and a GET can be repeated by the browser on its own.
     * <pre>PUT /api/v1/academic-years/1/current</pre>
     */
    @PutMapping("/{id}/current")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<AcademicYearResponse> makeCurrent(@PathVariable Long id) {
        return ApiResponse.ok(academicYearService.makeCurrent(id));
    }

    /**
     * Soft delete: the row stays for history, but the year is no longer listed.
     * Refused (400) while the year still has semesters.
     * <pre>DELETE /api/v1/academic-years/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        academicYearService.delete(id);
        return ApiResponse.deleted();
    }
}
