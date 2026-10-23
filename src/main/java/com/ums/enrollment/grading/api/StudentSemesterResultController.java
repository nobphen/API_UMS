package com.ums.enrollment.grading.api;

import com.ums.enrollment.grading.application.StudentSemesterResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student-semester-results")
@RequiredArgsConstructor
public class StudentSemesterResultController {

    private final StudentSemesterResultService studentSemesterResultService;
}
