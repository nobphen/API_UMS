package com.ums.people.application.impl;

import com.ums.people.application.GuardianService;
import com.ums.people.infrastructure.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: GuardianService using GuardianRepository. */
@Service
@Transactional
@RequiredArgsConstructor
public class GuardianServiceImpl implements GuardianService {

    private final GuardianRepository guardianRepository;
}
