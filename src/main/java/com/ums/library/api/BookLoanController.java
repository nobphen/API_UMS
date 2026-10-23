package com.ums.library.api;

import com.ums.library.application.BookLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/book-loans")
@RequiredArgsConstructor
public class BookLoanController {

    private final BookLoanService bookLoanService;
}
