package com.ums.people.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ums.ApiTestBase;
import org.junit.jupiter.api.Test;

/**
 * POST /api/v1/students checks everything before the first INSERT: a wrong form must not use up an id
 * in users, persons or students (no gaps like 1, 2, 4, 6 after a few mistakes). See ApiTestBase.
 */
class StudentCreateNoLostIdTest extends ApiTestBase {

    private static final String URL = "/api/v1/students";

    /** Five wrong forms in a row: every id counter stays where it was. */
    @Test
    void wrongFormsUseNoId() throws Exception {
        long programId = programId();
        create("NOGAP01", "555555551", "+855 12 555 551", programId);   // a student whose data the next forms reuse
        long[] before = lastIds();

        // 1. National ID already used
        refused(body("NOGAP02", "555555551", null, programId, null, ""),
                "National ID 555555551 is already used by another person");

        // 2. Phone already used by another login
        refused(body("NOGAP03", null, "+855 12 555 551", programId, null, ""),
                null);

        // 3. A bad address (no area and no text)
        refused(body("NOGAP04", null, null, programId, null, """
                , "addresses": [{"type": "CURRENT"}]"""), null);

        // 4. A batch of another program
        refused(body("NOGAP05", null, null, programId, -1L, ""),
                "This batch does not exist or does not belong to the student's program");

        // 5. A program that does not exist
        mockMvc.perform(asAdmin(post(URL)).content(body("NOGAP06", null, null, 999_999L, null, "")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Program not found"));

        assertThat(lastIds()).as("users, persons, students id counters").containsExactly(before);
    }

    /** A right form after the mistakes gets the very next id. */
    @Test
    void nextGoodFormGetsTheNextId() throws Exception {
        long programId = programId();
        long[] before = lastIds();

        refused(body("NOGAP11", "12", null, programId, null, ""), null);   // bad national ID: refused
        long id = create("NOGAP12", null, null, programId);

        assertThat(id).isEqualTo(before[2] + 1);
    }

    /** The last id given out in users, persons and students (PostgreSQL identity sequences). */
    private long[] lastIds() {
        return new long[] {
                jdbcTemplate.queryForObject("SELECT last_value FROM users_id_seq", Long.class),
                jdbcTemplate.queryForObject("SELECT last_value FROM persons_id_seq", Long.class),
                jdbcTemplate.queryForObject("SELECT last_value FROM students_id_seq", Long.class)};
    }

    /** The request is refused with 400 (and this message, when given). */
    private void refused(String json, String message) throws Exception {
        var result = mockMvc.perform(asAdmin(post(URL)).content(json)).andExpect(status().isBadRequest());
        if (message != null) {
            result.andExpect(jsonPath("$.message").value(message));
        }
    }

    /** Creates a student and returns the student id. */
    private long create(String code, String nationalId, String phone, long programId) throws Exception {
        String response = mockMvc.perform(asAdmin(post(URL)).content(body(code, nationalId, phone, programId, null, "")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(response, "$.data.student.id");
    }

    /** JSON for POST /students; extra is added at the end (e.g. the addresses). */
    private static String body(String code, String nationalId, String phone, long programId, Long batchId, String extra) {
        return """
                {"studentCode": "%s", "firstName": "Dara", "lastName": "Sok",
                 "nationalId": %s, "phone": %s, "programId": %d, "batchId": %s,
                 "admissionDate": "2026-10-01"%s}
                """.formatted(code, json(nationalId), json(phone), programId, batchId, extra);
    }

    /** "text" in quotes, or null. */
    private static String json(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    /** The sample program BSCS. */
    private long programId() {
        return jdbcTemplate.queryForObject("SELECT id FROM programs WHERE code = 'BSCS'", Long.class);
    }
}
