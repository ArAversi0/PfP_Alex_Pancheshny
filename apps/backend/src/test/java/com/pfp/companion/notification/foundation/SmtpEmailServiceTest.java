package com.pfp.companion.notification.foundation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.pfp.companion.notification.mediator.AuthEmailType;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpEmailServiceTest {

    private final JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
    private final SmtpEmailService service = new SmtpEmailService(mailSender, "noreply@pfp.local");

    @Test
    void sendsVerificationEmailWithActionLink() {
        Instant expiresAt = Instant.parse("2026-06-04T12:00:00Z");

        service.sendAuthEmail("hero@pfp.local", AuthEmailType.EMAIL_VERIFICATION,
                "http://localhost:5173/verify-email?token=abc", expiresAt);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();

        assertThat(message.getFrom()).isEqualTo("noreply@pfp.local");
        assertThat(message.getTo()).containsExactly("hero@pfp.local");
        assertThat(message.getSubject()).isEqualTo("PfP Companion System: verify your email");
        assertThat(message.getText())
                .contains("http://localhost:5173/verify-email?token=abc")
                .contains("2026-06-04T12:00:00Z");
    }
}
