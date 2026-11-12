package com.ums.hostel.application.impl;

import com.ums.hostel.application.HostelService;
import com.ums.hostel.infrastructure.HostelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Not written yet. Later: hostels and their warden.
 *
 * TODO when you build this: the warden of a hostel must not be deleted or leave (RESIGNED...).
 *      Add hostels.warden_staff_id (hostel not deleted) to StaffRepository.isDeanOrHead
 *      (people module, native SQL).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
}
