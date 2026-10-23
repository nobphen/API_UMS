package com.ums.enrollment.grading.api;

import com.ums.enrollment.grading.application.AssessmentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assessment-types")
@RequiredArgsConstructor
public class AssessmentTypeController {

    private final AssessmentTypeService assessmentTypeService;
}
