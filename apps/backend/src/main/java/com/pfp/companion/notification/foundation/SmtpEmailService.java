package com.pfp.companion.notification.foundation;

import com.pfp.companion.notification.mediator.AuthEmailType;
import com.pfp.companion.notification.mediator.EmailService;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pfp.mail.mode", havingValue = "smtp", matchIfMissing = true)
public final class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailService(JavaMailSender mailSender,
            @Value("${pfp.mail.from:noreply@pfp.local}") String from) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.from = Objects.requireNonNull(from);
    }

    @Override
    public void sendAuthEmail(String recipient, AuthEmailType type, String actionUrl,
            Instant expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(subject(type));
        message.setText(body(type, actionUrl, expiresAt));
        mailSender.send(message);
    }

    private static String subject(AuthEmailType type) {
        return switch (type) {
            case EMAIL_VERIFICATION, EMAIL_VERIFICATION_RESEND ->
                    "PfP Companion System: verify your email";
            case PASSWORD_RESET -> "PfP Companion System: reset your password";
        };
    }

    private static String body(AuthEmailType type, String actionUrl, Instant expiresAt) {
        String action = switch (type) {
            case EMAIL_VERIFICATION, EMAIL_VERIFICATION_RESEND -> "verify your email";
            case PASSWORD_RESET -> "reset your password";
        };
        return """
                Hello,

                Use this link to %s:
                %s

                This link expires at %s.

                If you did not request this email, you can ignore it.
                """.formatted(action, actionUrl, expiresAt);
    }
}
