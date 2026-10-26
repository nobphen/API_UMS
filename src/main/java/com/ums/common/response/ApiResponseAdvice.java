package com.ums.common.response;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Just before an {@link ApiResponse} is sent:
 * <ul>
 *   <li>sets {@code status} to the response's real HTTP status (from @ResponseStatus or ResponseEntity);</li>
 *   <li>when the message is a key (e.g. success.created), replaces it with the text in the caller's language
 *       (see LocaleConfig). Messages that are already text are left as they are.</li>
 * </ul>
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Pattern MESSAGE_KEY = Pattern.compile("[a-z0-9_]+(\\.[a-z0-9_]+)+");

    private final MessageSource messageSource;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof ApiResponse<?> api)) {
            return body;
        }
        int status = response instanceof ServletServerHttpResponse servlet ? servlet.getServletResponse().getStatus() : 200;
        String message = api.message();
        if (message != null && MESSAGE_KEY.matcher(message).matches()) {
            message = messageSource.getMessage(message, null, message, LocaleContextHolder.getLocale());
        }
        return api.with(status, message);
    }
}
