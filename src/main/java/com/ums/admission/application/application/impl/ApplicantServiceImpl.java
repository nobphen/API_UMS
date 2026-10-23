package com.ums.admission.application.application.impl;

import com.ums.admission.application.application.ApplicantService;
import com.ums.admission.application.infrastructure.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicantServiceImpl implements ApplicantService {

    private final ApplicantRepository applicantRepository;
}
