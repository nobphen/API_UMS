package com.ums.people.application.impl;

import com.ums.people.application.StaffService;
import com.ums.people.infrastructure.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: StaffService using StaffRepository. */
@Service
@Transactional
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
}
