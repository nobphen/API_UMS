package com.ums.engagement.alumni.application.impl;

import com.ums.engagement.alumni.application.AlumniProfileService;
import com.ums.engagement.alumni.infrastructure.AlumniProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlumniProfileServiceImpl implements AlumniProfileService {

    private final AlumniProfileRepository alumniProfileRepository;
}
