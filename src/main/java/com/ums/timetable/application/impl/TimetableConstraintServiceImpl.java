package com.ums.timetable.application.impl;

import com.ums.timetable.application.TimetableConstraintService;
import com.ums.timetable.infrastructure.TimetableConstraintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TimetableConstraintServiceImpl implements TimetableConstraintService {

    private final TimetableConstraintRepository timetableConstraintRepository;
}
