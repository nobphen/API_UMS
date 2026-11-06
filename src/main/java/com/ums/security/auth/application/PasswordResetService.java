package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.ForgotPasswordRequest;
import com.ums.security.auth.api.dto.ResetPasswordRequest;

/** "Forgot password?": a 6-digit code sent to the account's email, then a new password with that code. */
public interface PasswordResetService {

    /**
     * Sends a code if the account exists and has an email. Never says whether it exists, so accounts can't be guessed.
     * A user with no email (many students) must ask the office instead: UserService.resetPassword.
     *
     * @param request username, student code or email
     */
    void sendResetCode(ForgotPasswordRequest request);

    /**
     * Checks the code, sets the new password, unlocks a LOCKED account and ends all its sessions.
     * The code works only once.
     *
     * @param request the same username as in sendResetCode, the 6-digit code, newPassword, confirmPassword
     * @throws com.ums.common.exception.BusinessException error.password.mismatch;
     *                                                    error.reset.invalid_code (wrong or expired);
     *                                                    error.reset.too_many_attempts (the code is then deleted)
     */
    void resetPassword(ResetPasswordRequest request);
}
