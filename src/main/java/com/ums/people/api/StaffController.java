package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.NewStaffResponse;
import com.ums.people.api.dto.StaffCodeRequest;
import com.ums.people.api.dto.StaffRequest;
import com.ums.people.api.dto.StaffResponse;
import com.ums.people.api.dto.StaffStatusRequest;
import com.ums.people.api.dto.StaffUpdateRequest;
import com.ums.people.api.dto.StatusHistoryResponse;
import com.ums.people.application.StaffService;
import com.ums.people.domain.Staff;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Staff: lecturers and office workers. The admin adds and changes them (STAFF_MANAGE) and reads them (STAFF_VIEW);
 * a logged-in staff member reads their own record with /me.
 */
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /**
     * Adds a staff member: person + addresses + login + staff record. 201.
     * The response holds the temporary password once: print the login slip from it.
     * <pre>POST /api/v1/staff  {"staffCode": "LT2026001", "firstName": "Sokha", "lastName": "Chan",
     *                    "departmentId": 1, "staffType": "LECTURER", "hireDate": "2026-11-01", "addresses": [...]}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.STAFF_MANAGE)
    public ApiResponse<NewStaffResponse> create(@Valid @RequestBody StaffRequest request) {
        return ApiResponse.created(staffService.create(request));
    }

    /**
     * One page of staff for the admin table, deleted ones left out. Every filter is optional (empty = all);
     * search matches the staff code or the first / last name in English or Khmer.
     * Without page/size/sort: the first 20, ordered by staff code.
     * <pre>GET /api/v1/staff?search=chan&amp;departmentId=1&amp;staffType=LECTURER&amp;status=ACTIVE&amp;page=1&amp;size=20&amp;sort=staffCode,asc</pre>
     */
    @GetMapping
    @RequirePermission(PeoplePermissions.STAFF_VIEW)
    public ApiResponse<PageResponse<StaffResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Staff.StaffType staffType,   // LECTURER, OFFICE, BOTH
            @RequestParam(required = false) Staff.Status status,         // ACTIVE, ON_LEAVE, RESIGNED, TERMINATED, RETIRED
            @PageableDefault(size = 20, sort = "staffCode") Pageable pageable) {
        return ApiResponse.page(staffService.list(search, departmentId, staffType, status, pageable));
    }

    /**
     * Changes the staff member's personal details and job details.
     * <pre>PUT /api/v1/staff/5  {"firstName": "Sokha", "lastName": "Chan", "departmentId": 2,
     *                     "position": "Lecturer", "staffType": "BOTH"}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.STAFF_MANAGE)
    public ApiResponse<StaffResponse> update(@PathVariable Long id, @Valid @RequestBody StaffUpdateRequest request) {
        return ApiResponse.updated(staffService.update(id, request));
    }

    /**
     * Fixes a mistyped staff code; the login's username changes too when it was the old code (same password).
     * <pre>PUT /api/v1/staff/5/code  {"staffCode": "LT2026010"}</pre>
     */
    @PutMapping("/{id}/code")
    @RequirePermission(PeoplePermissions.STAFF_MANAGE)
    public ApiResponse<StaffResponse> changeCode(@PathVariable Long id, @Valid @RequestBody StaffCodeRequest request) {
        return ApiResponse.updated(staffService.changeCode(id, request.staffCode()));
    }

    /**
     * Every status change of the staff member with its reason, newest first.
     * <pre>GET /api/v1/staff/5/status-history</pre>
     */
    @GetMapping("/{id}/status-history")
    @RequirePermission(PeoplePermissions.STAFF_VIEW)
    public ApiResponse<List<StatusHistoryResponse>> statusHistory(@PathVariable Long id) {
        return ApiResponse.ok(staffService.getStatusHistory(id));
    }

    /**
     * A real job event: ON_LEAVE, back to ACTIVE, RESIGNED, TERMINATED or RETIRED. Leaving locks the login,
     * coming back unlocks it. The reason and the admin (from the token, never from the request)
     * are saved in staff_status_history.
     * <pre>POST /api/v1/staff/5/status  {"status": "RESIGNED", "reason": "Moved to another university"}</pre>
     */
    @PostMapping("/{id}/status")
    @RequirePermission(PeoplePermissions.STAFF_MANAGE)
    public ApiResponse<StaffResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StaffStatusRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        Long adminId = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        return ApiResponse.updated(staffService.changeStatus(id, request, adminId));
    }

    /**
     * Soft delete, only for an ACTIVE record made by mistake or twice (a staff member who left uses the status).
     * When the person has no other record, the person goes too and the login is locked.
     * The admin id comes from the token, never from the request, so nobody can fake who did it.
     * <pre>DELETE /api/v1/staff/5</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(PeoplePermissions.STAFF_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long adminId = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        staffService.delete(id, adminId);
        return ApiResponse.deleted();
    }

    /**
     * The logged-in staff member's own record. No id in the URL: the user id comes from the token
     * (jwt.getSubject()), so nobody can read someone else's record.
     * <pre>GET /api/v1/staff/me</pre>
     */
    @GetMapping("/me")
    public ApiResponse<StaffResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(staffService.getByUser(Long.valueOf(Objects.requireNonNull(jwt.getSubject()))));
    }

    /**
     * One staff member with person details; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/staff/5</pre>
     */
    @GetMapping("/{id}")
    @RequirePermission(PeoplePermissions.STAFF_VIEW)
    public ApiResponse<StaffResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(staffService.get(id));
    }
}
