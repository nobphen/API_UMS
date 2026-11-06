package com.ums.common.mail;

/**
 * Sends emails for every module (password reset, email verification, later receipts, admission letters...).
 * Emails are HTML (MimeMessage) with a plain-text copy for old mail apps. With no mail server set up
 * (spring.mail.host empty, e.g. on a developer PC) nothing is sent and only a warning is logged.
 */
public interface MailService {

    /**
     * Sends one HTML email. A sending error is logged, not thrown, so the caller's work (e.g. saving the
     * reset code) still goes on.
     *
     * @param to        the address; empty = nothing is sent
     * @param subject   the subject line, already in the user's language
     * @param html      the body as HTML
     * @param plainText the same text without HTML, shown by mail apps that can't show HTML
     */
    void send(String to, String subject, String html, String plainText);

    /**
     * Sends a 6-digit code in the standard UMS layout: title, short text, the code in a big box,
     * how long it is valid, and "ignore this email if it wasn't you".
     * Texts come from messages(_km).properties: mail.&lt;purpose&gt;.subject / .title / .intro / .ignore / .body.
     *
     * @param to       the address; empty = nothing is sent
     * @param language "km" or "en" (the user's saved language, because they are not in the app when it arrives)
     * @param purpose  which email, e.g. "password_reset" or "email_verify"
     * @param code     the 6 digits
     * @param minutes  how long the code stays valid
     */
    void sendCode(String to, String language, String purpose, String code, long minutes);
}
