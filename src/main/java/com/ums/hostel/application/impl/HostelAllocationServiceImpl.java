package com.ums.hostel.application.impl;

import com.ums.hostel.application.HostelAllocationService;
import com.ums.hostel.infrastructure.HostelAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HostelAllocationServiceImpl implements HostelAllocationService {

    private final HostelAllocationRepository hostelAllocationRepository;
}
