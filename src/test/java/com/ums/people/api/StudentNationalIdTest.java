package com.ums.people.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ums.ApiTestBase;
import org.junit.jupiter.api.Test;

/** One national ID = one person: checks create and update of a student (see ApiTestBase). */
class StudentNationalIdTest extends ApiTestBase {

    private static final String URL = "/api/v1/students";

    /** Creating a second student with a national ID that is already used is refused. */
    @Test
    void createWithTakenNationalIdIsRefused() throws Exception {
        create("NID001", "111111111");

        mockMvc.perform(asAdmin(post(URL)).content(createBody("NID002", "111111111")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("National ID 111111111 is already used by another person"));
    }

    /** Spaces and dashes don't trick the check: "111-111 112" is the same as "111111112". */
    @Test
    void createWithSameIdWrittenDifferentlyIsRefused() throws Exception {
        create("NID003", "111111112");

        mockMvc.perform(asAdmin(post(URL)).content(createBody("NID004", "111-111 112")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("National ID 111111112 is already used by another person"));
    }

    /** Changing a student's national ID to one another person has is refused. */
    @Test
    void updateToTakenNationalIdIsRefused() throws Exception {
        create("NID005", "222222221");
        long second = create("NID006", "222222222");

        mockMvc.perform(asAdmin(put(URL + "/" + second)).content(updateBody("222222221")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("National ID 222222221 is already used by another person"));
    }

    /** Saving a student with their OWN national ID again is fine (their own row is not counted). */
    @Test
    void updateKeepingOwnNationalIdIsAllowed() throws Exception {
        long id = create("NID007", "333333331");

        mockMvc.perform(asAdmin(put(URL + "/" + id)).content(updateBody("333333331")))
                .andExpect(status().isOk());
    }

    /** Creates a student in the sample program BSCS and returns the student id. */
    private long create(String code, String nationalId) throws Exception {
        String body = mockMvc.perform(asAdmin(post(URL)).content(createBody(code, nationalId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(body, "$.data.student.id");
    }

    /** JSON for POST /students, in the sample program BSCS. */
    private String createBody(String code, String nationalId) {
        Long programId = jdbcTemplate.queryForObject("SELECT id FROM programs WHERE code = 'BSCS'", Long.class);
        return """
                {"studentCode": "%s", "firstName": "Dara", "lastName": "Sok", "nationalId": "%s",
                 "programId": %d, "admissionDate": "2026-10-01"}
                """.formatted(code, nationalId, programId);
    }

    /** JSON for PUT /students/{id}. */
    private static String updateBody(String nationalId) {
        return """
                {"firstName": "Dara", "lastName": "Sok", "nationalId": "%s"}
                """.formatted(nationalId);
    }
}
