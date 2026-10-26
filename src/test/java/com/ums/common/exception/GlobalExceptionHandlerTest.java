package com.ums.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ums.common.response.ApiResponse;
import java.sql.SQLException;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.exc.InvalidFormatException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(messages());

    /** A broken database rule: CHECK / NOT NULL is wrong data (400), UNIQUE / foreign key is a conflict (409). */
    @ParameterizedTest
    @CsvSource({
            "23514, 400, Some fields are invalid",                  // CHECK, e.g. users.phone not +85512345678
            "23502, 400, Some fields are invalid",                  // NOT NULL
            "23505, 409, This record conflicts with existing data", // UNIQUE, e.g. same phone twice
            "23503, 409, This record conflicts with existing data"  // foreign key
    })
    void answersByTheKindOfDatabaseRule(String sqlState, int status, String message) {
        ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrity(brokenRule(sqlState));

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(message);
    }

    @Test
    void noSqlStateIsAConflict() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("no SQL error inside"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    /** An empty required field says which field: a name from field.* when there is one, else made from the Java name. */
    @Test
    void emptyRequiredFieldSaysWhichField() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(fieldError("username", "NotBlank"));
        result.addError(fieldError("device.platform", "NotNull"));
        result.addError(fieldError("programId", "NotNull"));
        result.addError(fieldError("studentCode", "NotBlank"));
        result.addError(new FieldError("request", "studentCode", null, false, new String[] {"Size"}, null, "size must be between 1 and 30"));
        result.addError(new FieldError("request", "email", "x", false, new String[] {"Email"}, null, "must be a well-formed email address"));
        MethodParameter parameter = new MethodParameter(getClass().getDeclaredMethod("emptyRequiredFieldSaysWhichField"), -1);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(new MethodArgumentNotValidException(parameter, result));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors())
                .containsEntry("username", "Username is required")
                .containsEntry("device.platform", "Platform is required")
                .containsEntry("programId", "Program ID is required")          // no field.programId here: made from the name
                .containsEntry("studentCode", "Student code is required")      // "required" wins over the Size problem
                .containsEntry("email", "must be a well-formed email address");  // other checks keep their own message
    }

    private static FieldError fieldError(String field, String code) {
        return new FieldError("request", field, null, false, new String[] {code}, null, "must not be blank");
    }

    /** A wrong value inside the JSON names its field, with dots and list positions, e.g. addresses[0].type. */
    @Test
    void wrongValueInTheBodyNamesTheField() {
        InvalidFormatException wrongValue = InvalidFormatException.from((JsonParser) null, "not one of the values", "NOWHERE", String.class);
        wrongValue.prependPath(new Object(), "type");
        wrongValue.prependPath(new Object(), 0);
        wrongValue.prependPath(new Object(), "addresses");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnreadableBody(
                new HttpMessageNotReadableException("JSON parse error", wrongValue, new MockHttpInputMessage(new byte[0])));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Some fields are invalid");
        assertThat(response.getBody().errors()).containsEntry("addresses[0].type", "Wrong value or type");
    }

    @Test
    void brokenJsonGetsTheGeneralMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnreadableBody(
                new HttpMessageNotReadableException("no body", new MockHttpInputMessage(new byte[0])));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("The request body is missing or is not valid JSON");
        assertThat(response.getBody().errors()).isNull();
    }

    /** As Spring throws it: the PostgreSQL error is a few causes down. */
    private static DataIntegrityViolationException brokenRule(String sqlState) {
        SQLException sql = new SQLException("rule broken", sqlState);
        return new DataIntegrityViolationException("could not execute statement", new RuntimeException(sql));
    }

    private static StaticMessageSource messages() {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("error.validation", Locale.getDefault(), "Some fields are invalid");
        messages.addMessage("error.conflict", Locale.getDefault(), "This record conflicts with existing data");
        messages.addMessage("error.invalid_value", Locale.getDefault(), "Wrong value or type");
        messages.addMessage("validation.required", Locale.getDefault(), "{0} is required");
        messages.addMessage("field.username", Locale.getDefault(), "Username");
        messages.addMessage("field.platform", Locale.getDefault(), "Platform");
        messages.addMessage("error.bad_request_body", Locale.getDefault(), "The request body is missing or is not valid JSON");
        return messages;
    }
}
