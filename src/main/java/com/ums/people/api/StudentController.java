package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.application.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /** The response holds the temporary password once: print the student's login slip from it. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(T(com.ums.people.PeoplePermissions).STUDENT_CREATE)")
    public ApiResponse<NewStudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ApiResponse.ok(studentService.create(request));
    }

    /** Master after Bachelor, PhD after Master, or a second degree: same person and login, new student record. */
    @PostMapping("/{id}/programs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(T(com.ums.people.PeoplePermissions).STUDENT_CREATE)")
    public ApiResponse<NewStudentResponse> addProgram(@PathVariable Long id, @Valid @RequestBody NewProgramRequest request) {
        return ApiResponse.ok(studentService.addProgram(id, request));
    }

    /** The logged-in student's programs, newest first. */
    @GetMapping("/me")
    public ApiResponse<List<StudentResponse>> myPrograms(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(studentService.getByUser(Long.valueOf(Objects.requireNonNull(jwt.getSubject()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.ums.people.PeoplePermissions).STUDENT_VIEW)")
    public ApiResponse<StudentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }
}
