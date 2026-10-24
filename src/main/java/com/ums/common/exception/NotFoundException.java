package com.ums.common.exception;

/** The record asked for does not exist (404). The message is a key, like BusinessException. */
public class NotFoundException extends BusinessException {

    public NotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
