package com.ums.hr.leave.application.impl;

import com.ums.hr.leave.application.StaffLeaveTypeService;
import com.ums.hr.leave.infrastructure.StaffLeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffLeaveTypeServiceImpl implements StaffLeaveTypeService {

    private final StaffLeaveTypeRepository staffLeaveTypeRepository;
}
