package com.ums.graduation.application.impl;

import com.ums.graduation.application.TranscriptRequestService;
import com.ums.graduation.infrastructure.TranscriptRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TranscriptRequestServiceImpl implements TranscriptRequestService {

    private final TranscriptRequestRepository transcriptRequestRepository;
}
