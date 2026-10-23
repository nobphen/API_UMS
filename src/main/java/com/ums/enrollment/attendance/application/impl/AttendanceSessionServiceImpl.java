package com.ums.enrollment.attendance.application.impl;

import com.ums.enrollment.attendance.application.AttendanceSessionService;
import com.ums.enrollment.attendance.infrastructure.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceSessionServiceImpl implements AttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;
}
