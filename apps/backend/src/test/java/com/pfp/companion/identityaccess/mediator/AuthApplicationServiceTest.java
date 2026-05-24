package com.pfp.companion.identityaccess.mediator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.entity.OAuthIdentity;
import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import com.pfp.companion.notification.mediator.AuthEmailLinkFactory;
import com.pfp.companion.notification.mediator.AuthEmailType;
import com.pfp.companion.notification.mediator.EmailService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-02T10:00:00Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthTokenRepository tokenRepository = mock(AuthTokenRepository.class);
    private final SecureTokenService secureTokenService = mock(SecureTokenService.class);
    private final AccessTokenService accessTokenService = mock(AccessTokenService.class);
    private final EmailService emailService = mock(EmailService.class);
    private final AuthProperties properties = new AuthProperties();
    private final AuthApplicationService service = new AuthApplicationService(userRepository,
            tokenRepository, secureTokenService, accessTokenService, new BCryptPasswordEncoder(),
            emailService, new AuthEmailLinkFactory(properties), properties,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void registersNormalizedUnverifiedAccountAndSendsVerificationLink() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(secureTokenService.generate()).thenReturn("raw-verification");
        when(secureTokenService.hash("raw-verification")).thenReturn("verification-hash");

        User user = service.register("  User@Example.COM ", "password", "password");

        assertThat(user.email()).isEqualTo("user@example.com");
        assertThat(user.emailVerified()).isFalse();
        assertThat(user.passwordHash()).startsWith("$2");
        verify(tokenRepository).saveEmailVerificationToken(user.id(), "verification-hash",
                NOW.plus(properties.getVerificationTokenLifetime()));
        verify(emailService).sendAuthEmail(eq("user@example.com"),
                eq(AuthEmailType.EMAIL_VERIFICATION),
                eq("http://localhost:5173/verify-email?token=raw-verification"), any());
    }

    @Test
    void blocksLoginUntilEmailIsVerified() {
        User user = user(false);
        when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(user.email(), "password"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("verification");
    }

    @Test
    void logsInVerifiedAccountAndPersistsHashedRefreshToken() {
        User user = user(true);
        when(userRepository.findByEmail(user.email())).thenReturn(Optional.of(user));
        when(secureTokenService.generate()).thenReturn("raw-refresh");
        when(secureTokenService.hash("raw-refresh")).thenReturn("refresh-hash");
        when(accessTokenService.generate(user)).thenReturn("access-jwt");

        AuthSession session = service.login(user.email(), "password");

        assertThat(session.accessToken()).isEqualTo("access-jwt");
        assertThat(session.refreshToken()).isEqualTo("raw-refresh");
        verify(tokenRepository).saveRefreshToken(user.id(), "refresh-hash",
                NOW.plus(properties.getRefreshTokenLifetime()));
    }

    @Test
    void rotatesRefreshToken() {
        User user = user(true);
        when(secureTokenService.generate()).thenReturn("replacement");
        when(secureTokenService.hash("current")).thenReturn("current-hash");
        when(secureTokenService.hash("replacement")).thenReturn("replacement-hash");
        when(tokenRepository.rotateRefreshToken("current-hash", "replacement-hash",
                NOW.plus(properties.getRefreshTokenLifetime()), NOW))
                .thenReturn(Optional.of(user.id()));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(accessTokenService.generate(user)).thenReturn("new-access");

        RefreshedTokens tokens = service.refresh("current");

        assertThat(tokens).isEqualTo(new RefreshedTokens("new-access", "replacement"));
    }

    @Test
    void resetsPasswordAndRevokesAllRefreshTokens() {
        User user = user(true);
        when(secureTokenService.hash("reset")).thenReturn("reset-hash");
        when(tokenRepository.consumePasswordResetToken("reset-hash", NOW))
                .thenReturn(Optional.of(user.id()));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(call -> call.getArgument(0));

        service.resetPassword("reset", "new-password", "new-password");

        verify(tokenRepository).revokeAllRefreshTokens(user.id(), NOW);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void linksVerifiedGoogleIdentityAndStoresHashedExchangeCode() {
        User localUser = user(false);
        when(userRepository.findByOAuthIdentity("google", "subject-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail(localUser.email())).thenReturn(Optional.of(localUser));
        when(userRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(secureTokenService.generate()).thenReturn("raw-exchange");
        when(secureTokenService.hash("raw-exchange")).thenReturn("exchange-hash");

        String exchangeCode = service.prepareOAuth2Exchange("google", "subject-1",
                localUser.email(), true);

        assertThat(exchangeCode).isEqualTo("raw-exchange");
        verify(userRepository).save(argThat(saved -> saved.emailVerified()
                && saved.oauthIdentities().contains(new OAuthIdentity("google", "subject-1"))));
        verify(tokenRepository).saveOAuthExchangeCode(localUser.id(), "exchange-hash",
                NOW.plus(properties.getOauth2ExchangeCodeLifetime()));
    }

    @Test
    void exchangesSingleUseOAuth2CodeForSession() {
        User user = user(true);
        when(secureTokenService.hash("one-time-code")).thenReturn("exchange-hash");
        when(tokenRepository.consumeOAuthExchangeCode("exchange-hash", NOW))
                .thenReturn(Optional.of(user.id()));
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));
        when(secureTokenService.generate()).thenReturn("raw-refresh");
        when(secureTokenService.hash("raw-refresh")).thenReturn("refresh-hash");
        when(accessTokenService.generate(user)).thenReturn("access-jwt");

        AuthSession session = service.exchangeOAuth2Code("one-time-code");

        assertThat(session.accessToken()).isEqualTo("access-jwt");
        assertThat(session.refreshToken()).isEqualTo("raw-refresh");
        verify(tokenRepository).saveRefreshToken(user.id(), "refresh-hash",
                NOW.plus(properties.getRefreshTokenLifetime()));
    }

    private static User user(boolean verified) {
        return new User(UUID.randomUUID(), "user@example.com",
                new BCryptPasswordEncoder().encode("password"), Role.ROLE_USER, verified, NOW,
                List.of());
    }
}
