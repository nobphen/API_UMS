package com.ums.enrollment.grading.api;

import com.ums.enrollment.grading.application.GradeScaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/grade-scales")
@RequiredArgsConstructor
public class GradeScaleController {

    private final GradeScaleService gradeScaleService;
}
