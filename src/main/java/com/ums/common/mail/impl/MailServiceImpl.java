package com.ums.common.mail.impl;

import com.ums.common.mail.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/** Sends the emails with JavaMailSender as MimeMessage (HTML + plain text, UTF-8 so Khmer shows correctly). */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    /**
     * The layout of every code email. Mail apps ignore &lt;style&gt; blocks, so the CSS is inline.
     * %1$s language, %2$s title, %3$s intro, %4$s code, %5$s "expires in", %6$s "ignore if not you", %7$s footer.
     */
    private static final String CODE_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="%1$s">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:24px;background:#f4f5f7;font-family:'Battambang','Khmer OS',Arial,sans-serif;color:#1f2937">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px;margin:0 auto;background:#ffffff;border-radius:8px">
                <tr><td style="padding:20px 24px;background:#1e3a8a;color:#ffffff;font-size:20px;font-weight:bold;border-radius:8px 8px 0 0">UMS</td></tr>
                <tr><td style="padding:24px">
                  <h2 style="margin:0 0 12px;font-size:20px">%2$s</h2>
                  <p style="margin:0 0 20px;line-height:1.6">%3$s</p>
                  <div style="margin:0 0 20px;padding:16px;text-align:center;background:#eef2ff;border-radius:8px;font-size:32px;font-weight:bold;letter-spacing:8px;color:#1e3a8a">%4$s</div>
                  <p style="margin:0 0 8px;line-height:1.6">%5$s</p>
                  <p style="margin:0;line-height:1.6;color:#6b7280;font-size:13px">%6$s</p>
                </td></tr>
                <tr><td style="padding:16px 24px;border-top:1px solid #e5e7eb;color:#9ca3af;font-size:12px">%7$s</td></tr>
              </table>
            </body>
            </html>
            """;

    private final ObjectProvider<JavaMailSender> mailSender;   // only exists when spring.mail.host is set
    private final MessageSource messageSource;
    private final String mailFrom;

    /** Spring passes the mail sender (if any), the messages and ums.mail.from from application.yml. */
    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                           MessageSource messageSource,
                           @Value("${ums.mail.from}") String mailFrom) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.mailFrom = mailFrom;
    }

    /** No address or no mail server → warning only; otherwise builds the MimeMessage and sends it; errors are logged. */
    @Override
    public void send(String to, String subject, String html, String plainText) {
        if (to == null || to.isBlank()) {
            log.warn("Email \"{}\" not sent: no address", subject);
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Email \"{}\" not sent: no mail server configured (spring.mail.host)", subject);
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            // true = multipart: the plain-text and HTML versions travel together (and attachments later)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            sender.send(message);
        } catch (MessagingException | MailException ex) {
            log.error("Could not send email \"{}\"", subject, ex);
        }
    }

    /** Reads the texts in the user's language, puts them in CODE_TEMPLATE (HTML-escaped), then send(). */
    @Override
    public void sendCode(String to, String language, String purpose, String code, long minutes) {
        Locale locale = Locale.forLanguageTag(language == null || language.isBlank() ? "en" : language);
        String prefix = "mail." + purpose + ".";
        String html = CODE_TEMPLATE.formatted(
                locale.getLanguage(),
                text(prefix + "title", locale),
                text(prefix + "intro", locale),
                HtmlUtils.htmlEscape(code),
                HtmlUtils.htmlEscape(messageSource.getMessage("mail.code.expires", new Object[] {minutes}, locale)),
                text(prefix + "ignore", locale),
                text("mail.footer", locale));
        String plainText = messageSource.getMessage(prefix + "body", new Object[] {code, minutes}, locale);
        send(to, messageSource.getMessage(prefix + "subject", null, locale), html, plainText);
    }

    /** One text from messages(_km).properties, made safe to put in HTML (&lt; &gt; &amp; escaped). */
    private String text(String key, Locale locale) {
        return HtmlUtils.htmlEscape(messageSource.getMessage(key, null, locale));
    }
}
