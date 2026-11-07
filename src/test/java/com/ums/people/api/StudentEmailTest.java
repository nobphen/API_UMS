package com.ums.people.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ums.ApiTestBase;
import org.junit.jupiter.api.Test;

/** The email rule (common/util/Emails) on POST /api/v1/students: a domain ending like .com is required. */
class StudentEmailTest extends ApiTestBase {

    private static final String URL = "/api/v1/students";

    /** No dot after @ ("dara.sok@gmail"): refused with the email message, in English and Khmer. */
    @Test
    void emailWithoutDomainEndingIsRefused() throws Exception {
        mockMvc.perform(asAdmin(post(URL)).content(body("MAIL01", "dara.sok@gmail")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", containsString("must be a valid email address")));

        mockMvc.perform(asAdmin(post(URL), "km").content(body("MAIL02", "dara.sok@gmail")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", containsString("ត្រូវជាអ៊ីមែលត្រឹមត្រូវ")));
    }

    /** Other wrong shapes are refused too. */
    @Test
    void otherBadEmailsAreRefused() throws Exception {
        for (String email : new String[] {"dara.sok", "dara sok@gmail.com", "dara@@gmail.com", "dara@gmail.c"}) {
            mockMvc.perform(asAdmin(post(URL)).content(body("MAIL03", email)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());
        }
    }

    /** Normal emails, a Cambodian .edu.kh one, and no email at all (it is optional) are saved. */
    @Test
    void goodOrEmptyEmailsAreSaved() throws Exception {
        mockMvc.perform(asAdmin(post(URL)).content(body("MAIL11", "dara.sok11@gmail.com")))
                .andExpect(status().isCreated());
        mockMvc.perform(asAdmin(post(URL)).content(body("MAIL12", "dara12@rupp.edu.kh")))
                .andExpect(status().isCreated());
        mockMvc.perform(asAdmin(post(URL)).content(body("MAIL13", "")))
                .andExpect(status().isCreated());
    }

    /** JSON for POST /students in the sample program BSCS. */
    private String body(String code, String email) {
        Long programId = jdbcTemplate.queryForObject("SELECT id FROM programs WHERE code = 'BSCS'", Long.class);
        return """
                {"studentCode": "%s", "firstName": "Dara", "lastName": "Sok", "email": "%s",
                 "programId": %d, "admissionDate": "2026-10-01"}
                """.formatted(code, email, programId);
    }
}
