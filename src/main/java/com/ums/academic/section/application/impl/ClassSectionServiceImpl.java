package com.ums.academic.section.application.impl;

import com.ums.academic.section.application.ClassSectionService;
import com.ums.academic.section.infrastructure.ClassSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Not written yet. Later: class sections (one course in one semester) and their lecturers.
 *
 * TODO when you build this: a lecturer of an OPEN class must not be deleted or leave (RESIGNED...).
 *      Add class_section_lecturers (only classes with status OPEN, not deleted) to
 *      StaffRepository.isDeanOrHead (people module, native SQL).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ClassSectionServiceImpl implements ClassSectionService {

    private final ClassSectionRepository classSectionRepository;
}
