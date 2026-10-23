package com.ums.admission.application.application.impl;

import com.ums.admission.application.application.ApplicationService;
import com.ums.admission.application.infrastructure.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
}
