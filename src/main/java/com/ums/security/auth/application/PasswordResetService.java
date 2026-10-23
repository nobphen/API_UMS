package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.ForgotPasswordRequest;
import com.ums.security.auth.api.dto.ResetPasswordRequest;

/** "Forgot password?": a 6-digit code sent to the account's email, then a new password with that code. */
public interface PasswordResetService {

    /** Sends a code if the account exists and has an email. Never says whether it exists, so accounts can't be guessed. */
    void sendResetCode(ForgotPasswordRequest request);

    /** Checks the code, sets the new password, unlocks a LOCKED account and ends all its sessions. */
    void resetPassword(ResetPasswordRequest request);
}
