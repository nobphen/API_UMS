package com.ums.people.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ums.ApiTestBase;
import org.junit.jupiter.api.Test;

/** DELETE /api/v1/students/{id}: soft delete of a record made by mistake (see ApiTestBase). */
class StudentDeleteApiTest extends ApiTestBase {

    private static final String URL = "/api/v1/students";

    /** An ACTIVE record is deleted; GET and a second DELETE then give 404. */
    @Test
    void deleteActiveStudent() throws Exception {
        long id = create("DEL001", "444444441");

        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isOk());

        mockMvc.perform(asAdmin(get(URL + "/" + id)))
                .andExpect(status().isNotFound());
        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isNotFound());
    }

    /** The only record of the person: the person is soft deleted and the login locked; the national ID is free again. */
    @Test
    void lastRecordDeletesPersonAndLocksLogin() throws Exception {
        long id = create("DEL002", "444444442");

        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isOk());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT p.deleted_at IS NOT NULL FROM persons p JOIN students s ON s.person_id = p.id WHERE s.id = ?",
                Boolean.class, id)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT u.status FROM users u JOIN students s ON s.user_id = u.id WHERE s.id = ?",
                String.class, id)).isEqualTo("LOCKED");

        // the right record can be added again with the same national ID
        create("DEL003", "444444442");
    }

    /** A student who really left (DROPPED) is kept: 400, change the status instead. */
    @Test
    void notActiveIsRefused() throws Exception {
        long id = create("DEL004", "444444444");
        mockMvc.perform(asAdmin(post(URL + "/" + id + "/status")).content("""
                        {"status": "DROPPED", "reason": "Left school"}
                        """))
                .andExpect(status().isOk());

        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Only an ACTIVE record made by mistake can be deleted; this student is DROPPED, change the status instead"));
    }

    /** Creates a student in the sample program BSCS and returns the student id. */
    private long create(String code, String nationalId) throws Exception {
        Long programId = jdbcTemplate.queryForObject("SELECT id FROM programs WHERE code = 'BSCS'", Long.class);
        String body = mockMvc.perform(asAdmin(post(URL)).content("""
                        {"studentCode": "%s", "firstName": "Dara", "lastName": "Sok", "nationalId": "%s",
                         "programId": %d, "admissionDate": "2026-10-01"}
                        """.formatted(code, nationalId, programId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(body, "$.data.student.id");
    }
}
