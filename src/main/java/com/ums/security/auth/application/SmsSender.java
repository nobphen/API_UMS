package com.ums.security.auth.application;

/**
 * Sends a text message. Replace LogOnlySmsSender with a class for the chosen SMS gateway
 * (a local Cambodian gateway, Twilio…) to deliver the login codes for real.
 */
public interface SmsSender {

    /**
     * @param phone normalized phone number, e.g. +85512345678
     * @param text  the message, already in the user's language
     */
    void send(String phone, String text);
}
