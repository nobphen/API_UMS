package com.ums.graduation.application.impl;

import com.ums.graduation.application.GraduationApplicationService;
import com.ums.graduation.infrastructure.GraduationApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GraduationApplicationServiceImpl implements GraduationApplicationService {

    private final GraduationApplicationRepository graduationApplicationRepository;
}
