package com.ums.library.api;

import com.ums.library.application.LibraryFineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library-fines")
@RequiredArgsConstructor
public class LibraryFineController {

    private final LibraryFineService libraryFineService;
}
