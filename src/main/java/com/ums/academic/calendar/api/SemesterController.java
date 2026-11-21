package com.ums.academic.calendar.api;

import com.ums.academic.AcademicPermissions;
import com.ums.academic.calendar.api.dto.SemesterRequest;
import com.ums.academic.calendar.api.dto.SemesterResponse;
import com.ums.academic.calendar.application.SemesterService;
import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Semesters (e.g. "Semester 1" of 2026-2027): the terms inside an academic year.
 * Reading is open to every logged-in user (dropdowns, timetable, student app).
 * Creating, changing, making current and deleting need SEMESTER_MANAGE.
 */
@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    /**
     * New semester, not current yet. 201. Number free in that year (deleted semesters count too),
     * start before end, inside the year, no day shared with another semester of the year.
     * Registration dates are optional (send both or none).
     * <pre>
     * POST /api/v1/semesters
     * {"academicYearId": 1, "name": "Semester 1", "nameKh": "ឆមាសទី១", "semesterNo": 1,
     *  "startDate": "2025-10-01", "endDate": "2026-02-28",
     *  "registrationStart": "2025-09-15", "registrationEnd": "2025-10-10"}
     * </pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<SemesterResponse> create(@Valid @RequestBody SemesterRequest request) {
        return ApiResponse.created(semesterService.create(request));
    }

    /**
     * The semesters of one year (not deleted), in order 1, 2, 3.
     * <pre>GET /api/v1/semesters?academicYearId=1</pre>
     */
    @GetMapping
    public ApiResponse<List<SemesterResponse>> list(@RequestParam Long academicYearId) {
        return ApiResponse.ok(semesterService.list(academicYearId));
    }

    /**
     * The current semester; 404 when no semester is current yet.
     * <pre>GET /api/v1/semesters/current</pre>
     */
    @GetMapping("/current")
    public ApiResponse<SemesterResponse> getCurrent() {
        return ApiResponse.ok(semesterService.getCurrent());
    }

    /**
     * One semester; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/semesters/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<SemesterResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(semesterService.get(id));
    }

    /**
     * Replaces every field (send all, like for create). "current" is not changed here.
     * <pre>
     * PUT /api/v1/semesters/1
     * {"academicYearId": 1, "name": "Semester 1", "nameKh": "ឆមាសទី១", "semesterNo": 1,
     *  "startDate": "2025-10-01", "endDate": "2026-03-15",
     *  "registrationStart": "2025-09-15", "registrationEnd": "2025-10-10"}
     * </pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<SemesterResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody SemesterRequest request) {
        return ApiResponse.updated(semesterService.update(id, request));
    }

    /**
     * Makes this semester the current one; the old current semester is switched off. No body.
     * Its academic year must be the current year (400).
     * <pre>PUT /api/v1/semesters/1/current</pre>
     */
    @PutMapping("/{id}/current")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<SemesterResponse> makeCurrent(@PathVariable Long id) {
        return ApiResponse.ok(semesterService.makeCurrent(id));
    }

    /**
     * Soft delete: the row stays for history, but the semester is no longer listed.
     * Refused (400) while the semester is current (make another semester current first) or still has class sections.
     * <pre>DELETE /api/v1/semesters/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(AcademicPermissions.SEMESTER_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        semesterService.delete(id);
        return ApiResponse.deleted();
    }
}
