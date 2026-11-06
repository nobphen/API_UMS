package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.QrLoginResponse;
import java.util.Optional;

/**
 * QR login: the web page shows a QR code, the logged-in mobile app scans and approves it,
 * and the web page then receives its own tokens. Codes live in Redis for 2 minutes.
 */
public interface QrLoginService {

    /**
     * Called by the web page (not logged in): makes a new QR code that waits for approval.
     *
     * @return the qrId (put it in the QR picture) and how many seconds it stays valid
     */
    QrLoginResponse create();

    /**
     * Called by the mobile app (already logged in) after scanning.
     *
     * @param qrId   the id read from the QR picture
     * @param userId the logged-in mobile user (from the token)
     * @throws com.ums.common.exception.BusinessException error.qr.expired_or_used
     */
    void approve(String qrId, Long userId);

    /**
     * The approving user's id once approved (single use), empty while still pending.
     *
     * @param qrId id from create
     * @return the user id once approved; empty while waiting
     * @throws com.ums.common.exception.BusinessException error.qr.expired / error.qr.used
     */
    Optional<Long> consumeApproved(String qrId);
}
