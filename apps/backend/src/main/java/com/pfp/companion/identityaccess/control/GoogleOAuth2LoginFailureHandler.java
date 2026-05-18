package com.pfp.companion.identityaccess.control;

import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public final class GoogleOAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final AuthProperties properties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public GoogleOAuth2LoginFailureHandler(AuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String callbackUrl = UriComponentsBuilder.fromUriString(properties.getOauth2FrontendCallbackUrl())
                .queryParam("error", "oauth2_login_failed")
                .build()
                .encode()
                .toUriString();
        redirectStrategy.sendRedirect(request, response, callbackUrl);
    }
}
