package com.ums.hostel.application.impl;

import com.ums.hostel.application.HostelService;
import com.ums.hostel.infrastructure.HostelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
}
