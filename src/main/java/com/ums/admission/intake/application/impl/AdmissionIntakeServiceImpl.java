package com.ums.admission.intake.application.impl;

import com.ums.admission.intake.application.AdmissionIntakeService;
import com.ums.admission.intake.infrastructure.AdmissionIntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdmissionIntakeServiceImpl implements AdmissionIntakeService {

    private final AdmissionIntakeRepository admissionIntakeRepository;
}
