package com.ums.hr.leave.api;

import com.ums.hr.leave.application.StaffLeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff-leave-requests")
@RequiredArgsConstructor
public class StaffLeaveRequestController {

    private final StaffLeaveRequestService staffLeaveRequestService;
}
