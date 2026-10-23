package com.ums.timetable.api;

import com.ums.timetable.application.TimetableRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/timetable-runs")
@RequiredArgsConstructor
public class TimetableRunController {

    private final TimetableRunService timetableRunService;
}
