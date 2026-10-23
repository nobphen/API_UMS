package com.ums.library.application.impl;

import com.ums.library.application.LibraryFineService;
import com.ums.library.infrastructure.LibraryFineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LibraryFineServiceImpl implements LibraryFineService {

    private final LibraryFineRepository libraryFineRepository;
}
