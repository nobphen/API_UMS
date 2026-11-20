package com.ums.common.numbering.impl;

import com.ums.common.numbering.DocumentNumberService;
import com.ums.common.numbering.DocumentType;
import com.ums.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Makes document numbers from the document_sequences table, one row per prefix and year. */
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentNumberServiceImpl implements DocumentNumberService {

    /** One statement: adds the row for a new prefix or year, or adds 1 to it. The row lock stops duplicates. */
    private static final String NEXT_VALUE = """
            INSERT INTO document_sequences (prefix, year, last_value) VALUES (?, ?, 1)
            ON CONFLICT (prefix, year) DO UPDATE SET last_value = document_sequences.last_value + 1
            RETURNING last_value
            """;

    private final JdbcTemplate jdbcTemplate;

    /** This year (Cambodia time) + one SQL statement that adds 1 to the counter → e.g. INV-2026-00015. */
    @Override
    public String next(DocumentType type) {
        int year = DateUtils.currentYear();   // Cambodia's calendar, not the server's (UTC)
        Long value = jdbcTemplate.queryForObject(NEXT_VALUE, Long.class, type.getPrefix(), year);
        return "%s-%d-%05d".formatted(type.getPrefix(), year, value);
    }
}
