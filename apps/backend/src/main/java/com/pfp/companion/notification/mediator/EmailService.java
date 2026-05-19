package com.pfp.companion.notification.mediator;

import java.time.Instant;

public interface EmailService {

    void sendAuthEmail(String recipient, AuthEmailType type, String actionUrl, Instant expiresAt);
}
