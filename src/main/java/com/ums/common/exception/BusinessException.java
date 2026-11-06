package com.ums.common.exception;

/**
 * A rule the request broke (400). The message is a key in i18n/messages*.properties, filled with args and
 * written in the caller's language by GlobalExceptionHandler, e.g. ("error.student.code_exists", "ST2026001").
 * An arg can itself be a MessageSourceResolvable, so words inside a message are translated too.
 */
public class BusinessException extends RuntimeException {

    private final transient Object[] args;

    /**
     * @param messageKey the i18n key, e.g. error.student.code_exists
     * @param args       values put into the message's {0}, {1}, ...
     */
    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    /** The values for the message's {0}, {1}, ... */
    public Object[] getArgs() {
        return args;
    }
}
