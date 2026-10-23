package com.ums.library.api;

import com.ums.library.application.BookReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/book-reservations")
@RequiredArgsConstructor
public class BookReservationController {

    private final BookReservationService bookReservationService;
}
