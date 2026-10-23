package com.ums.hr.leave.application.impl;

import com.ums.hr.leave.application.StaffLeaveRequestService;
import com.ums.hr.leave.infrastructure.StaffLeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffLeaveRequestServiceImpl implements StaffLeaveRequestService {

    private final StaffLeaveRequestRepository staffLeaveRequestRepository;
}
