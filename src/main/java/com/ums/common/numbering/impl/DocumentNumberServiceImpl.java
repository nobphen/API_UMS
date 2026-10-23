package com.ums.common.numbering.impl;

import com.ums.common.numbering.DocumentNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentNumberServiceImpl implements DocumentNumberService {
}
