package com.ums.people.application.impl;

import com.ums.people.application.PersonService;
import com.ums.people.infrastructure.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: PersonService using PersonRepository. */
@Service
@Transactional
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
}
