package com.ums.finance.fee.application.impl;

import com.ums.finance.fee.application.FeeStructureService;
import com.ums.finance.fee.infrastructure.FeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FeeStructureServiceImpl implements FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
}
