package com.ums.academic.course.application.impl;

import com.ums.academic.course.application.CurriculumService;
import com.ums.academic.course.infrastructure.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumRepository curriculumRepository;
}
