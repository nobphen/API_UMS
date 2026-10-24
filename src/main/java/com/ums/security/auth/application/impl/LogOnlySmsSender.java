package com.ums.security.auth.application.impl;

import com.ums.security.auth.application.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** No SMS gateway is set up yet: nothing is sent. In dev, ums.otp.log-code writes the code to the log instead. */
@Slf4j
@Component
public class LogOnlySmsSender implements SmsSender {

    @Override
    public void send(String phone, String text) {
        log.warn("SMS to {} not sent: no SMS gateway configured (replace LogOnlySmsSender)", phone);
    }
}
