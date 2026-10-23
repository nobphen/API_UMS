package com.ums.library.api;

import com.ums.library.application.LibraryCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library-categories")
@RequiredArgsConstructor
public class LibraryCategoryController {

    private final LibraryCategoryService libraryCategoryService;
}
