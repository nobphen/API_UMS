package com.ums.hostel.application.impl;

import com.ums.hostel.application.HostelRoomService;
import com.ums.hostel.infrastructure.HostelRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HostelRoomServiceImpl implements HostelRoomService {

    private final HostelRoomRepository hostelRoomRepository;
}
