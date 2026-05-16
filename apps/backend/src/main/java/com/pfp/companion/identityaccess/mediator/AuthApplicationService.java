package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.entity.OAuthIdentity;
import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import com.pfp.companion.notification.mediator.AuthEmailLinkFactory;
import com.pfp.companion.notification.mediator.AuthEmailType;
import com.pfp.companion.notification.mediator.EmailService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public final class AuthApplicationService {

    public static final String FORGOT_PASSWORD_MESSAGE =
            "If the account exists, a reset email has been sent.";
    public static final String RESEND_VERIFICATION_MESSAGE =
            "If an unverified account exists, a verification email has been sent.";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final SecureTokenService secureTokenService;
    private final AccessTokenService accessTokenService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthEmailLinkFactory linkFactory;
    private final AuthProperties properties;
    private final Clock clock;

    public AuthApplicationService(UserRepository userRepository, AuthTokenRepository tokenRepository,
            SecureTokenService secureTokenService, AccessTokenService accessTokenService,
            PasswordEncoder passwordEncoder, EmailService emailService, AuthEmailLinkFactory linkFactory,
            AuthProperties properties, Clock clock) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.secureTokenService = secureTokenService;
        this.accessTokenService = accessTokenService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.linkFactory = linkFactory;
        this.properties = properties;
        this.clock = clock;
    }

    public User register(String email, String password, String confirmPassword) {
        validatePassword(password, confirmPassword);
        String normalizedEmail = normalizedEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException();
        }
        User user = userRepository.save(new User(UUID.randomUUID(), normalizedEmail,
                passwordEncoder.encode(password), Role.ROLE_USER, false, clock.instant(), List.of()));
        sendVerificationEmail(user, AuthEmailType.EMAIL_VERIFICATION);
        return user;
    }

    public void verifyEmail(String rawToken) {
        UUID userId = tokenRepository.consumeEmailVerificationToken(
                        secureTokenService.hash(rawToken), clock.instant())
                .orElseThrow(() -> new AuthException("verification token is invalid or expired"));
        User user = requireUser(userId);
        if (!user.emailVerified()) {
            userRepository.save(user.verified());
        }
    }

    public String resendVerification(String email) {
        userRepository.findByEmail(normalizedEmail(email))
                .filter(user -> !user.emailVerified())
                .ifPresent(user -> sendVerificationEmail(user, AuthEmailType.EMAIL_VERIFICATION_RESEND));
        return RESEND_VERIFICATION_MESSAGE;
    }

    public AuthSession login(String email, String password) {
        User user = userRepository.findByEmail(normalizedEmail(email))
                .orElseThrow(AuthApplicationService::invalidCredentials);
        if (user.passwordHash() == null || !passwordEncoder.matches(password, user.passwordHash())) {
            throw invalidCredentials();
        }
        if (!user.emailVerified()) {
            throw new AuthException("email verification is required");
        }
        return createSession(user);
    }

    public RefreshedTokens refresh(String rawRefreshToken) {
        Instant now = clock.instant();
        String replacement = secureTokenService.generate();
        UUID userId = tokenRepository.rotateRefreshToken(secureTokenService.hash(rawRefreshToken),
                        secureTokenService.hash(replacement),
                        now.plus(properties.getRefreshTokenLifetime()), now)
                .orElseThrow(() -> new AuthException("refresh token is invalid or expired"));
        User user = requireVerifiedUser(userId);
        return new RefreshedTokens(accessTokenService.generate(user), replacement);
    }

    public void logout(String rawRefreshToken) {
        tokenRepository.revokeRefreshToken(secureTokenService.hash(rawRefreshToken), clock.instant());
    }

    public String prepareOAuth2Exchange(String provider, String providerSubject, String email,
            boolean emailVerified) {
        if (!"google".equals(provider)) {
            throw new AuthException("OAuth2 provider is not supported");
        }
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new AuthException("OAuth2 subject is unavailable");
        }
        if (!emailVerified) {
            throw new AuthException("OAuth2 provider email must be verified");
        }
        String normalizedEmail = normalizedEmail(email);
        OAuthIdentity identity = new OAuthIdentity(provider, providerSubject);
        User user = userRepository.findByOAuthIdentity(provider, providerSubject)
                .orElseGet(() -> linkOrCreateOAuth2User(normalizedEmail, identity));
        if (!user.emailVerified()) {
            user = userRepository.save(user.verified());
        }
        String exchangeCode = secureTokenService.generate();
        tokenRepository.saveOAuthExchangeCode(user.id(), secureTokenService.hash(exchangeCode),
                clock.instant().plus(properties.getOauth2ExchangeCodeLifetime()));
        return exchangeCode;
    }

    public AuthSession exchangeOAuth2Code(String rawCode) {
        UUID userId = tokenRepository.consumeOAuthExchangeCode(secureTokenService.hash(rawCode),
                        clock.instant())
                .orElseThrow(() -> new AuthException("OAuth2 exchange code is invalid or expired"));
        return createSession(requireVerifiedUser(userId));
    }

    public String forgotPassword(String email) {
        userRepository.findByEmail(normalizedEmail(email)).ifPresent(user -> {
            try {
                sendPasswordResetEmail(user);
            } catch (EmailDeliveryException exception) {
                LOGGER.error("Could not send password reset email for user {}", user.id(), exception);
            }
        });
        return FORGOT_PASSWORD_MESSAGE;
    }

    public void resetPassword(String rawToken, String password, String confirmPassword) {
        validatePassword(password, confirmPassword);
        Instant now = clock.instant();
        UUID userId = tokenRepository.consumePasswordResetToken(secureTokenService.hash(rawToken), now)
                .orElseThrow(() -> new AuthException("password reset token is invalid or expired"));
        User user = requireUser(userId);
        userRepository.save(user.withPasswordHash(passwordEncoder.encode(password)));
        tokenRepository.revokeAllRefreshTokens(userId, now);
    }

    public User requireUser(UUID userId) {
        return userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new AuthException("user account is unavailable"));
    }

    private AuthSession createSession(User user) {
        String refreshToken = secureTokenService.generate();
        tokenRepository.saveRefreshToken(user.id(), secureTokenService.hash(refreshToken),
                clock.instant().plus(properties.getRefreshTokenLifetime()));
        return new AuthSession(accessTokenService.generate(user), refreshToken, user);
    }

    private User linkOrCreateOAuth2User(String normalizedEmail, OAuthIdentity identity) {
        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> existing.withOAuthIdentity(identity).verified())
                .orElseGet(() -> new User(UUID.randomUUID(), normalizedEmail, null, Role.ROLE_USER,
                        true, clock.instant(), List.of(identity)));
        return userRepository.save(user);
    }

    private void sendVerificationEmail(User user, AuthEmailType type) {
        Instant now = clock.instant();
        String rawToken = secureTokenService.generate();
        tokenRepository.revokeEmailVerificationTokens(user.id(), now);
        tokenRepository.saveEmailVerificationToken(user.id(), secureTokenService.hash(rawToken),
                now.plus(properties.getVerificationTokenLifetime()));
        try {
            emailService.sendAuthEmail(user.email(), type, linkFactory.verificationLink(rawToken),
                    now.plus(properties.getVerificationTokenLifetime()));
        } catch (RuntimeException exception) {
            throw new EmailDeliveryException(
                    "account exists but verification email could not be delivered", exception);
        }
    }

    private void sendPasswordResetEmail(User user) {
        Instant now = clock.instant();
        String rawToken = secureTokenService.generate();
        tokenRepository.revokePasswordResetTokens(user.id(), now);
        tokenRepository.savePasswordResetToken(user.id(), secureTokenService.hash(rawToken),
                now.plus(properties.getPasswordResetTokenLifetime()));
        try {
            emailService.sendAuthEmail(user.email(), AuthEmailType.PASSWORD_RESET,
                    linkFactory.passwordResetLink(rawToken),
                    now.plus(properties.getPasswordResetTokenLifetime()));
        } catch (RuntimeException exception) {
            throw new EmailDeliveryException("password reset email could not be delivered", exception);
        }
    }

    private User requireVerifiedUser(UUID userId) {
        User user = requireUser(userId);
        if (!user.emailVerified()) {
            throw new AuthException("email verification is required");
        }
        return user;
    }

    private static void validatePassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("password must contain at least 8 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("password confirmation does not match");
        }
    }

    private static String normalizedEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static AuthException invalidCredentials() {
        return new AuthException("invalid email or password");
    }
}
