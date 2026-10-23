package com.ums.admission.formsale.application.impl;

import com.ums.admission.formsale.application.ApplicationFormSaleService;
import com.ums.admission.formsale.infrastructure.ApplicationFormSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationFormSaleServiceImpl implements ApplicationFormSaleService {

    private final ApplicationFormSaleRepository applicationFormSaleRepository;
}
