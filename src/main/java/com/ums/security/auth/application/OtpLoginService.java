package com.ums.security.auth.application;

/** Login with a 6-digit code sent by SMS to the phone number saved on the account. */
public interface OtpLoginService {

    /**
     * Sends a new code to the phone. Says nothing about whether the number has an account, so nobody can use
     * it to find out who studies here; unknown numbers, inactive accounts and repeats within a minute are ignored.
     *
     * @param phone phone number as typed
     */
    void sendCode(String phone);

    /**
     * Checks the code and uses it up.
     *
     * @param phone phone number as typed
     * @param code  the 6 digits from the SMS
     * @return id of the user the phone belongs to
     * @throws com.ums.common.exception.BusinessException error.otp.invalid_code or error.otp.too_many_attempts
     */
    Long verifyCode(String phone, String code);
}
