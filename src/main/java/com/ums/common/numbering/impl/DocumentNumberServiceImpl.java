package com.ums.common.numbering.impl;

import com.ums.common.numbering.DocumentNumberService;
import com.ums.common.numbering.DocumentType;
import java.time.Year;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentNumberServiceImpl implements DocumentNumberService {

    /** The year on the number follows Cambodia's calendar, not the server's (UTC). */
    private static final ZoneId ZONE = ZoneId.of("Asia/Phnom_Penh");

    /** One statement: adds the row for a new prefix or year, or adds 1 to it. The row lock stops duplicates. */
    private static final String NEXT_VALUE = """
            INSERT INTO document_sequences (prefix, year, last_value) VALUES (?, ?, 1)
            ON CONFLICT (prefix, year) DO UPDATE SET last_value = document_sequences.last_value + 1
            RETURNING last_value
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String next(DocumentType type) {
        int year = Year.now(ZONE).getValue();
        Long value = jdbcTemplate.queryForObject(NEXT_VALUE, Long.class, type.getPrefix(), year);
        return "%s-%d-%05d".formatted(type.getPrefix(), year, value);
    }
}
