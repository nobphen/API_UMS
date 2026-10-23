package com.ums.hr.payroll.api;

import com.ums.hr.payroll.application.TeachingLoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teaching-loads")
@RequiredArgsConstructor
public class TeachingLoadController {

    private final TeachingLoadService teachingLoadService;
}
