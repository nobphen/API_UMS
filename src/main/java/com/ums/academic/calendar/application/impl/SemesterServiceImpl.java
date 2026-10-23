package com.ums.academic.calendar.application.impl;

import com.ums.academic.calendar.application.SemesterService;
import com.ums.academic.calendar.infrastructure.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;
}
