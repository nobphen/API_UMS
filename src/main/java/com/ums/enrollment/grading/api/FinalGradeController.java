package com.ums.enrollment.grading.api;

import com.ums.enrollment.grading.application.FinalGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/final-grades")
@RequiredArgsConstructor
public class FinalGradeController {

    private final FinalGradeService finalGradeService;
}
