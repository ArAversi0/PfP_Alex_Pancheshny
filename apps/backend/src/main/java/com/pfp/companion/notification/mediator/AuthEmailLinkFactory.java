package com.pfp.companion.notification.mediator;

import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public final class AuthEmailLinkFactory {

    private final AuthProperties properties;

    public AuthEmailLinkFactory(AuthProperties properties) {
        this.properties = properties;
    }

    public String verificationLink(String rawToken) {
        return link("/verify-email", rawToken);
    }

    public String passwordResetLink(String rawToken) {
        return link("/reset-password", rawToken);
    }

    private String link(String path, String rawToken) {
        return properties.getFrontendBaseUrl() + path + "?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }
}
