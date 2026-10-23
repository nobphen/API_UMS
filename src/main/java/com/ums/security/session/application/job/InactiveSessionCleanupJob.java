package com.ums.security.session.application.job;

import com.ums.security.session.application.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Nightly: marks sessions that expired or went unused too long as ended, so they leave the "Devices" list. */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveSessionCleanupJob {

    private final UserSessionService userSessionService;

    @Scheduled(cron = "${ums.session.cleanup-cron:0 0 3 * * *}")
    public void run() {
        int ended = userSessionService.revokeExpiredAndInactive();
        log.info("Session cleanup: {} expired or inactive sessions ended", ended);
    }
}
