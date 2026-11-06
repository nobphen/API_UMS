package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.PromoteStudentsRequest;
import com.ums.people.api.dto.PromoteStudentsResponse;
import com.ums.people.api.dto.StudentCodeRequest;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.api.dto.StudentStatusRequest;
import com.ums.people.api.dto.StudentUpdateRequest;
import com.ums.people.api.dto.YearLevelRequest;
import com.ums.people.application.StudentService;
import com.ums.people.domain.Student;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
 * Students. One person can have several student records (Bachelor, then Master ...), all with the same login.
 * The registrar adds students (STUDENT_CREATE) and moves them up a year (STUDENT_UPDATE);
 * a logged-in student reads their own records with /me.
 */
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * Adds a student: person + addresses + login + student record. 201.
     * The response holds the temporary password once: print the student's login slip from it.
     * <pre>POST /api/v1/students  {"studentCode": "ST2026001", "firstName": "Dara", "lastName": "Sok",
     *                       "programId": 1, "admissionDate": "2026-10-15", "addresses": [...]}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.STUDENT_CREATE)
    public ApiResponse<NewStudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ApiResponse.ok(studentService.create(request));
    }

    /**
     * Master after Bachelor, PhD after Master, or a second degree: same person and login, new student record. 201.
     * <pre>POST /api/v1/students/15/programs  {"studentCode": "MS2030014", "programId": 2, "admissionDate": "2030-10-15"}</pre>
     */
    @PostMapping("/{id}/programs")
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.STUDENT_CREATE)
    public ApiResponse<NewStudentResponse> addProgram(@PathVariable Long id, @Valid @RequestBody NewProgramRequest request) {
        return ApiResponse.ok(studentService.addProgram(id, request));
    }

    /**
     * One page of students for the admin table, deleted ones left out. Every filter is optional (empty = all);
     * search matches the student code or the first / last name in English or Khmer.
     * Without page/size/sort: the first 20, ordered by student code.
     * <pre>GET /api/v1/students?search=chan&amp;programId=1&amp;batchId=1&amp;yearLevel=2&amp;status=ACTIVE&amp;page=1&amp;size=20&amp;sort=studentCode,asc</pre>
     */
    @GetMapping
    @RequirePermission(PeoplePermissions.STUDENT_VIEW)
    public ApiResponse<PageResponse<StudentResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer yearLevel,
            @RequestParam(required = false) Student.Status status,   // ACTIVE, SUSPENDED, ON_LEAVE, GRADUATED, DROPPED
            @PageableDefault(size = 20, sort = "studentCode") Pageable pageable) {
        return ApiResponse.page(studentService.list(search, programId, batchId, yearLevel, status, pageable));
    }

    /**
     * End of the academic year: move a program's (or batch's) students up one year; list the ones repeating it.
     * <pre>POST /api/v1/students/promote  {"programId": 1, "batchId": 1, "fromYear": 1, "keepStudentIds": [15, 27]}
     * → {"promoted": 38, "kept": 2}</pre>
     */
    @PostMapping("/promote")
    @RequirePermission(PeoplePermissions.STUDENT_UPDATE)
    public ApiResponse<PromoteStudentsResponse> promote(@Valid @RequestBody PromoteStudentsRequest request) {
        return ApiResponse.ok(studentService.promote(request));
    }

    /**
     * Changes the student's personal details, batch and expected graduation year.
     * <pre>PUT /api/v1/students/15  {"firstName": "Dara", "lastName": "Sok", "gender": "MALE",
     *                         "phone": "012345678", "batchId": 1, "expectedGraduationYear": 2030}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.STUDENT_UPDATE)
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ApiResponse.ok(studentService.update(id, request));
    }

    /**
     * Sets one student's year by hand, e.g. after a credit transfer or a mistake.
     * <pre>PUT /api/v1/students/15/year-level  {"yearLevel": 3}</pre>
     */
    @PutMapping("/{id}/year-level")
    @RequirePermission(PeoplePermissions.STUDENT_UPDATE)
    public ApiResponse<StudentResponse> setYearLevel(@PathVariable Long id, @Valid @RequestBody YearLevelRequest request) {
        return ApiResponse.ok(studentService.setYearLevel(id, request.yearLevel()));
    }

    /**
     * A real event: SUSPENDED, ON_LEAVE, back to ACTIVE, GRADUATED or DROPPED. Returns the student after the change.
     * <pre>POST /api/v1/students/15/status  {"status": "GRADUATED", "reason": "Finished Bachelor 2030"}</pre>
     */
    @PostMapping("/{id}/status")
    @RequirePermission(PeoplePermissions.STUDENT_UPDATE)
    public ApiResponse<StudentResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StudentStatusRequest request) {
        return ApiResponse.ok(studentService.changeStatus(id, request));
    }

    /**
     * Fixes a mistyped student code; the login's username changes too when it was the old code (same password).
     * <pre>PUT /api/v1/students/15/code  {"studentCode": "ST2026010"}</pre>
     */
    @PutMapping("/{id}/code")
    @RequirePermission(PeoplePermissions.STUDENT_UPDATE)
    public ApiResponse<StudentResponse> changeCode(@PathVariable Long id, @Valid @RequestBody StudentCodeRequest request) {
        return ApiResponse.ok(studentService.changeCode(id, request.studentCode()));
    }

    /**
     * The logged-in student's programs, newest first. No id in the URL: the user id comes from the token
     * (jwt.getSubject()), so nobody can read someone else's records.
     * <pre>GET /api/v1/students/me</pre>
     */
    @GetMapping("/me")
    public ApiResponse<List<StudentResponse>> myPrograms(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(studentService.getByUser(Long.valueOf(Objects.requireNonNull(jwt.getSubject()))));
    }

    /**
     * One student with person details and addresses; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/students/15</pre>
     */
    @GetMapping("/{id}")
    @RequirePermission(PeoplePermissions.STUDENT_VIEW)
    public ApiResponse<StudentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }
}
