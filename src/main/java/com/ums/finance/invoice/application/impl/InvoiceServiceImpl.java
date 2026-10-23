package com.ums.finance.invoice.application.impl;

import com.ums.finance.invoice.application.InvoiceService;
import com.ums.finance.invoice.infrastructure.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
}
