package com.ums.security.auth.application;

import com.ums.security.user.domain.User;

/** After sign-up: a 6-digit code sent to the email; the account stays PENDING (can't log in) until it is typed in. */
public interface EmailVerificationService {

    /**
     * Sends a code to a PENDING account. Used by register; at most one email per minute.
     * The code is saved in Redis as a hash, never in plain text.
     *
     * @param user the new PENDING account (must have an email)
     */
    void sendCode(User user);

    /**
     * "I didn't get the code": sends a new one. Never says whether the email exists, so accounts can't be guessed.
     *
     * @param email the email used at sign-up
     */
    void resendCode(String email);

    /**
     * Checks the code and makes the account ACTIVE, so the applicant can log in. The code works only once.
     *
     * @param email the email used at sign-up
     * @param code  the 6 digits from the email
     * @throws com.ums.common.exception.BusinessException error.verify.invalid_code (wrong, expired, or no PENDING account);
     *                                                    error.verify.too_many_attempts (the code is then deleted)
     */
    void verify(String email, String code);
}
