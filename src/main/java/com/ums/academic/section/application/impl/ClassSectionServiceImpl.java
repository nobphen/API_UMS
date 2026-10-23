package com.ums.academic.section.application.impl;

import com.ums.academic.section.application.ClassSectionService;
import com.ums.academic.section.infrastructure.ClassSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassSectionServiceImpl implements ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
}
