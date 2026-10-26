package com.ums.organization.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ums.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * The faculty APIs from the outside, like Postman or the Flutter app (see ApiTestBase).
 * Every test makes its own codes, so the tests don't depend on each other's data.
 */
class FacultyApiTest extends ApiTestBase {

    private static final String URL = "/api/v1/faculties";

    // ---------- create ----------

    @Test
    void createSavesTheCodeInCapitals() throws Exception {
        mockMvc.perform(asAdmin(post(URL)).content("""
                        {"code": " eng1 ", "name": "Faculty of Engineering", "nameKh": "មហាវិទ្យាល័យវិស្វកម្ម"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Created successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.code").value("ENG1"))
                .andExpect(jsonPath("$.data.nameKh").value("មហាវិទ្យាល័យវិស្វកម្ម"));
    }

    @Test
    void sameCodeTwiceIsRefused() throws Exception {
        create("DUP1", "First");

        mockMvc.perform(asAdmin(post(URL)).content("""
                        {"code": "dup1", "name": "Second"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Faculty code already exists: DUP1"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void emptyFieldsAreNamed() throws Exception {
        mockMvc.perform(asAdmin(post(URL)).content("""
                        {"code": "", "name": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Some fields are invalid"))
                .andExpect(jsonPath("$.errors.code").value("Code is required"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"));
    }

    @Test
    void khmerMessagesWithAcceptLanguageKm() throws Exception {
        mockMvc.perform(asAdmin(post(URL), "km").content("""
                        {"code": "", "name": "X"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("សូមបំពេញលេខកូដ"));
    }

    // ---------- read ----------

    @Test
    void getOne() throws Exception {
        long id = create("GET1", "Faculty to read");

        mockMvc.perform(asAdmin(get(URL + "/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("GET1"))
                .andExpect(jsonPath("$.data.name").value("Faculty to read"));
    }

    @Test
    void unknownIdIsNotFound() throws Exception {
        mockMvc.perform(asAdmin(get(URL + "/999999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Faculty not found"));
    }

    @Test
    void listSearchesAndPages() throws Exception {
        create("SRCH1", "Search Alpha");
        create("SRCH2", "Search Beta");

        // page=1 is the first page (one-indexed-parameters), and the answer says page 1 too
        mockMvc.perform(asAdmin(get(URL)).param("search", "srch").param("page", "1").param("size", "1").param("sort", "code,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("SRCH1"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));

        mockMvc.perform(asAdmin(get(URL)).param("search", "srch").param("page", "2").param("size", "1").param("sort", "code,asc"))
                .andExpect(jsonPath("$.data.content[0].code").value("SRCH2"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void noPageMeansTheFirstPage() throws Exception {
        create("NOPAGE1", "No page given");

        mockMvc.perform(asAdmin(get(URL)).param("search", "NOPAGE1"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.content[0].code").value("NOPAGE1"));
    }

    @Test
    void unknownSortFieldIsABadRequest() throws Exception {
        mockMvc.perform(asAdmin(get(URL)).param("sort", "nothing,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sort").value("Wrong value or type"));
    }

    // ---------- update ----------

    @Test
    void updateChangesTheFields() throws Exception {
        long id = create("UPD1", "Old name");

        mockMvc.perform(asAdmin(put(URL + "/" + id)).content("""
                        {"code": "UPD1", "name": "New name", "nameKh": "   "}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated successfully"))
                .andExpect(jsonPath("$.data.name").value("New name"))
                .andExpect(jsonPath("$.data.nameKh").doesNotExist());   // blank → null
    }

    @Test
    void updateToAnotherFacultysCodeIsRefused() throws Exception {
        create("TAKEN1", "Has the code");
        long id = create("MINE1", "Wants the code");

        mockMvc.perform(asAdmin(put(URL + "/" + id)).content("""
                        {"code": "TAKEN1", "name": "Wants the code"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Faculty code already exists: TAKEN1"));
    }

    // ---------- delete ----------

    @Test
    void deleteHidesTheFacultyButKeepsTheRow() throws Exception {
        long id = create("DEL1", "To delete");

        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(asAdmin(get(URL + "/" + id))).andExpect(status().isNotFound());
        mockMvc.perform(asAdmin(get(URL)).param("search", "DEL1"))
                .andExpect(jsonPath("$.data.content[*].code", not(hasItem("DEL1"))));

        // soft delete: the row is still in the table, with deleted_at set
        Integer kept = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM faculties WHERE id = ? AND deleted_at IS NOT NULL", Integer.class, id);
        assertThat(kept).isEqualTo(1);
    }

    @Test
    void deletedFacultysCodeCantBeUsedAgain() throws Exception {
        long id = create("REUSE1", "Deleted one");
        mockMvc.perform(asAdmin(delete(URL + "/" + id))).andExpect(status().isOk());

        mockMvc.perform(asAdmin(post(URL)).content("""
                        {"code": "REUSE1", "name": "New one"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Faculty code already exists: REUSE1"));
    }

    @Test
    void facultyWithDepartmentsCantBeDeleted() throws Exception {
        long id = create("HASDEP1", "Has a department");
        jdbcTemplate.update("INSERT INTO departments (faculty_id, code, name) VALUES (?, 'HASDEP1-D', 'A department')", id);

        mockMvc.perform(asAdmin(delete(URL + "/" + id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This faculty still has departments. Delete or move them first"));
    }

    // ---------- security ----------

    @Test
    void noTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Please log in first"));
    }

    // ---------- helpers ----------

    /** Creates a faculty through the API and returns its id. */
    private long create(String code, String name) throws Exception {
        String body = mockMvc.perform(asAdmin(post(URL)).content("""
                        {"code": "%s", "name": "%s"}
                        """.formatted(code, name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(body, "$.data.id");
    }
}
