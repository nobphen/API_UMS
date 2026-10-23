package com.ums.hostel.api;

import com.ums.hostel.application.HostelAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hostel-allocations")
@RequiredArgsConstructor
public class HostelAllocationController {

    private final HostelAllocationService hostelAllocationService;
}
