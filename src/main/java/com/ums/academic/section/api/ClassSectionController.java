package com.ums.academic.section.api;

import com.ums.academic.section.application.ClassSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/class-sections")
@RequiredArgsConstructor
public class ClassSectionController {

    private final ClassSectionService classSectionService;
}
