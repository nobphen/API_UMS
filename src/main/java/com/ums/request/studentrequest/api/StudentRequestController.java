package com.ums.request.studentrequest.api;

import com.ums.request.studentrequest.application.StudentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student-requests")
@RequiredArgsConstructor
public class StudentRequestController {

    private final StudentRequestService studentRequestService;
}
