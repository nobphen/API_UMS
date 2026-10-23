package com.ums.timetable.api;

import com.ums.timetable.application.TimetableConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/timetable-constraints")
@RequiredArgsConstructor
public class TimetableConstraintController {

    private final TimetableConstraintService timetableConstraintService;
}
