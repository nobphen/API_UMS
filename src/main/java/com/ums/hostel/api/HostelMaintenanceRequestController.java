package com.ums.hostel.api;

import com.ums.hostel.application.HostelMaintenanceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hostel-maintenance-requests")
@RequiredArgsConstructor
public class HostelMaintenanceRequestController {

    private final HostelMaintenanceRequestService hostelMaintenanceRequestService;
}
