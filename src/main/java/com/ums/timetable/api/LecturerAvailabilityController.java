package com.ums.timetable.api;

import com.ums.timetable.application.LecturerAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lecturer-availabilities")
@RequiredArgsConstructor
public class LecturerAvailabilityController {

    private final LecturerAvailabilityService lecturerAvailabilityService;
}
