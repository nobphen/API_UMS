package com.ums.library.application.impl;

import com.ums.library.application.BookReservationService;
import com.ums.library.infrastructure.BookReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookReservationServiceImpl implements BookReservationService {

    private final BookReservationRepository bookReservationRepository;
}
