package com.ums.timetable.application.impl;

import com.ums.timetable.application.LecturerAvailabilityService;
import com.ums.timetable.infrastructure.LecturerAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LecturerAvailabilityServiceImpl implements LecturerAvailabilityService {

    private final LecturerAvailabilityRepository lecturerAvailabilityRepository;
}
