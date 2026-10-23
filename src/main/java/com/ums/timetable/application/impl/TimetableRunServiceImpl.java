package com.ums.timetable.application.impl;

import com.ums.timetable.application.TimetableRunService;
import com.ums.timetable.infrastructure.TimetableRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableRunServiceImpl implements TimetableRunService {

    private final TimetableRunRepository timetableRunRepository;
}
