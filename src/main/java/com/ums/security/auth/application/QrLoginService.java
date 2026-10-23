package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.QrLoginResponse;
import java.util.Optional;

/**
 * QR login: the web page shows a QR code, the logged-in mobile app scans and approves it,
 * and the web page then receives its own tokens. Codes live in Redis for a short time.
 */
public interface QrLoginService {

    QrLoginResponse create();

    /** Called by the mobile app (already logged in) after scanning. */
    void approve(String qrId, Long userId);

    /** The approving user's id once approved (single use), empty while still pending. */
    Optional<Long> consumeApproved(String qrId);
}
