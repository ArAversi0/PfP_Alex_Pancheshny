package com.pfp.companion.identityaccess.control;

import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import com.pfp.companion.identityaccess.mediator.AuthApplicationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public final class GoogleOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuth2LoginSuccessHandler.class);

    private final AuthApplicationService authService;
    private final AuthProperties properties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public GoogleOAuth2LoginSuccessHandler(AuthApplicationService authService,
            AuthProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauth2 = requireGoogleAuthentication(authentication);
            OidcUser user = requireOidcUser(oauth2);
            String exchangeCode = authService.prepareOAuth2Exchange("google", user.getSubject(),
                    user.getEmail(), Boolean.TRUE.equals(user.getEmailVerified()));
            redirectStrategy.sendRedirect(request, response, callbackUrl("code", exchangeCode));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not complete Google OAuth2 login", exception);
            redirectStrategy.sendRedirect(request, response,
                    callbackUrl("error", "oauth2_login_failed"));
        }
    }

    private static OAuth2AuthenticationToken requireGoogleAuthentication(
            Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2
                && "google".equals(oauth2.getAuthorizedClientRegistrationId())) {
            return oauth2;
        }
        throw new IllegalArgumentException("Google OAuth2 authentication is required");
    }

    private static OidcUser requireOidcUser(OAuth2AuthenticationToken authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser;
        }
        throw new IllegalArgumentException("Google OpenID Connect user is required");
    }

    private String callbackUrl(String parameter, String value) {
        return UriComponentsBuilder.fromUriString(properties.getOauth2FrontendCallbackUrl())
                .queryParam(parameter, value)
                .build()
                .encode()
                .toUriString();
    }
}
