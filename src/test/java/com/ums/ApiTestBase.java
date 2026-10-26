package com.ums;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Base class for API tests: the whole app with a real PostgreSQL and Redis in Docker (Testcontainers),
 * called like Postman or the Flutter app (security, validation, services, database).
 * Every API test class extends it, so they all share one app and one database: Docker starts once per run.
 * <pre>
 * class DepartmentApiTest extends ApiTestBase {
 *     &#64;Test
 *     void create() throws Exception {
 *         mockMvc.perform(asAdmin(post("/api/v1/departments")).content("{…}"))
 *                .andExpect(status().isCreated());
 *     }
 * }
 * </pre>
 * The data stays between tests of one run: give each test its own codes (DUP1, UPD1 …).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mockMvc;

    /** For setting up or checking rows directly, e.g. a department to block a delete, or deleted_at. */
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /** The admin's access token, new before every test. */
    protected String token;

    /** Logs in as the sample admin (db/testdata), who has every permission through SUPER_ADMIN. */
    @BeforeEach
    void logInAsAdmin() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "Admin@12345",
                                 "device": {"platform": "WEB", "deviceId": "api-test"}}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(body, "$.data.accessToken");
    }

    /** The request with the admin's token, JSON body and English messages. */
    protected MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return asAdmin(request, "en");
    }

    /** The request with the admin's token, JSON body and messages in this language (en or km). */
    protected MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request, String language) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /** The id in a JSON answer, e.g. idOf(body, "$.data.id"). */
    protected static long idOf(String json, String path) {
        return ((Number) JsonPath.read(json, path)).longValue();
    }
}
