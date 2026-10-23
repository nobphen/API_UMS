package com.ums.graduation.api;

import com.ums.graduation.application.GraduationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/graduation-applications")
@RequiredArgsConstructor
public class GraduationApplicationController {

    private final GraduationApplicationService graduationApplicationService;
}
