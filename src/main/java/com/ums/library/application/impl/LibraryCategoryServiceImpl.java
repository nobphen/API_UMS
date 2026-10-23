package com.ums.library.application.impl;

import com.ums.library.application.LibraryCategoryService;
import com.ums.library.infrastructure.LibraryCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LibraryCategoryServiceImpl implements LibraryCategoryService {

    private final LibraryCategoryRepository libraryCategoryRepository;
}
