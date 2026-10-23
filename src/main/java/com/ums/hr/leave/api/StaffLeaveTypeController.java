package com.ums.hr.leave.api;

import com.ums.hr.leave.application.StaffLeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff-leave-types")
@RequiredArgsConstructor
public class StaffLeaveTypeController {

    private final StaffLeaveTypeService staffLeaveTypeService;
}
