package com.ums.finance.scholarship.api;

import com.ums.finance.scholarship.application.StudentScholarshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student-scholarships")
@RequiredArgsConstructor
public class StudentScholarshipController {

    private final StudentScholarshipService studentScholarshipService;
}
