package com.pfp.companion.notification.foundation;

import com.pfp.companion.notification.mediator.AuthEmailType;
import com.pfp.companion.notification.mediator.EmailService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pfp.mail.mode", havingValue = "log")
public final class LoggingEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendAuthEmail(String recipient, AuthEmailType type, String actionUrl,
            Instant expiresAt) {
        LOGGER.info("Auth email: type={}, recipient={}, subject={}, expiresAt={}, actionUrl={}",
                type, recipient, subject(type), expiresAt, actionUrl);
    }

    private static String subject(AuthEmailType type) {
        return switch (type) {
            case EMAIL_VERIFICATION, EMAIL_VERIFICATION_RESEND ->
                    "PfP Companion System: verify your email";
            case PASSWORD_RESET -> "PfP Companion System: reset your password";
        };
    }
}
