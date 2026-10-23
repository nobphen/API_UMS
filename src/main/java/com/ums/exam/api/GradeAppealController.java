package com.ums.exam.api;

import com.ums.exam.application.GradeAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/grade-appeals")
@RequiredArgsConstructor
public class GradeAppealController {

    private final GradeAppealService gradeAppealService;
}
