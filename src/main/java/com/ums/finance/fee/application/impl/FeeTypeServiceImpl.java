package com.ums.finance.fee.application.impl;

import com.ums.finance.fee.application.FeeTypeService;
import com.ums.finance.fee.infrastructure.FeeTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FeeTypeServiceImpl implements FeeTypeService {

    private final FeeTypeRepository feeTypeRepository;
}
