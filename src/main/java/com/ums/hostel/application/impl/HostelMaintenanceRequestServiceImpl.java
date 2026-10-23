package com.ums.hostel.application.impl;

import com.ums.hostel.application.HostelMaintenanceRequestService;
import com.ums.hostel.infrastructure.HostelMaintenanceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HostelMaintenanceRequestServiceImpl implements HostelMaintenanceRequestService {

    private final HostelMaintenanceRequestRepository hostelMaintenanceRequestRepository;
}
