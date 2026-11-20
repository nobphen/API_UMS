package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.CurrentUser;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.GuardianRequest;
import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.GuardianUpdateRequest;
import com.ums.people.application.GuardianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Guardians (parents) of students. Every endpoint needs GUARDIAN_MANAGE (REGISTRAR),
 * also the GETs: a guardian's phone and email are private.
 * Version 1: a guardian has no login; linking to students comes later.
 */
@RestController
@RequestMapping("/api/v1/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    /**
     * Adds a guardian: person + addresses + guardian record. 201.
     * <pre>POST /api/v1/guardians  {"firstName": "Dara", "lastName": "Sok", "phone": "012345678",
     *                        "occupation": "Farmer", "addresses": [...]}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.GUARDIAN_MANAGE)
    public ApiResponse<GuardianResponse> create(@Valid @RequestBody GuardianRequest request) {
        return ApiResponse.created(guardianService.create(request));
    }

    /**
     * One guardian; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/guardians/5</pre>
     */
    @GetMapping("/{id}")
    @RequirePermission(PeoplePermissions.GUARDIAN_MANAGE)
    public ApiResponse<GuardianResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(guardianService.get(id));
    }

    /**
     * One page of guardians, deleted ones left out. search matches the first / last name
     * (English or Khmer) or the phone. Without page/size/sort: the first 20, ordered by last name.
     * <pre>GET /api/v1/guardians?search=sok&amp;page=1&amp;size=20&amp;sort=person.lastName,asc</pre>
     */
    @GetMapping
    @RequirePermission(PeoplePermissions.GUARDIAN_MANAGE)
    public ApiResponse<PageResponse<GuardianResponse>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "person.lastName", size = 20) Pageable pageable
    ) {
        return ApiResponse.page(guardianService.list(search, pageable));
    }

    /**
     * Changes the person details and the occupation. Addresses: PUT /persons/{personId}/addresses.
     * <pre>PUT /api/v1/guardians/5  {"firstName": "Dara", "lastName": "Sok", "occupation": "Teacher"}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.GUARDIAN_MANAGE)
    public ApiResponse<GuardianResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody GuardianUpdateRequest request
    ) {
        return ApiResponse.updated(guardianService.update(id, request));
    }

    /**
     * Soft delete, for a record made by mistake or twice. Its student links are removed.
     * The admin id comes from the token, for the log.
     * <pre>DELETE /api/v1/guardians/5</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(PeoplePermissions.GUARDIAN_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long adminId = CurrentUser.userId(jwt);
        guardianService.delete(id, adminId);
        return ApiResponse.deleted();
    }
}
