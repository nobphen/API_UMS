package com.ums.common.exception;

/** The record asked for does not exist (404). The message is a key, like BusinessException. */
public class NotFoundException extends BusinessException {

    /**
     * @param messageKey the i18n key, e.g. error.student.not_found
     * @param args       values put into the message's {0}, {1}, ...
     */
    public NotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
