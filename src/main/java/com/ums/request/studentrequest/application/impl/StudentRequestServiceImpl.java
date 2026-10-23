package com.ums.request.studentrequest.application.impl;

import com.ums.request.studentrequest.application.StudentRequestService;
import com.ums.request.studentrequest.infrastructure.StudentRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentRequestServiceImpl implements StudentRequestService {

    private final StudentRequestRepository studentRequestRepository;
}
