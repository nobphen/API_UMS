package com.ums.common.exception;

/**
 * A rule the request broke (400). The message is a key in i18n/messages*.properties, filled with args and
 * written in the caller's language by GlobalExceptionHandler, e.g. ("error.student.code_exists", "ST2026001").
 * An arg can itself be a MessageSourceResolvable, so words inside a message are translated too.
 */
public class BusinessException extends RuntimeException {

    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
