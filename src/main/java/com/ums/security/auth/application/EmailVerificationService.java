package com.ums.security.auth.application;

import com.ums.security.user.domain.User;

/** After sign-up: a 6-digit code sent to the email; the account stays PENDING (can't log in) until it is typed in. */
public interface EmailVerificationService {

    /** Sends a code to a PENDING account. Used by register; at most one email per minute. */
    void sendCode(User user);

    /** "I didn't get the code": sends a new one. Never says whether the email exists, so accounts can't be guessed. */
    void resendCode(String email);

    /** Checks the code and makes the account ACTIVE, so the applicant can log in. */
    void verify(String email, String code);
}
