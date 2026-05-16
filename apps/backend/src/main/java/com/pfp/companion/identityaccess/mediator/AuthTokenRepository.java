package com.pfp.companion.identityaccess.mediator;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository {

    void saveEmailVerificationToken(UUID userId, String tokenHash, Instant expiresAt);

    Optional<UUID> consumeEmailVerificationToken(String tokenHash, Instant now);

    void revokeEmailVerificationTokens(UUID userId, Instant now);

    void savePasswordResetToken(UUID userId, String tokenHash, Instant expiresAt);

    Optional<UUID> consumePasswordResetToken(String tokenHash, Instant now);

    void revokePasswordResetTokens(UUID userId, Instant now);

    void saveRefreshToken(UUID userId, String tokenHash, Instant expiresAt);

    Optional<UUID> rotateRefreshToken(String currentTokenHash, String replacementTokenHash,
            Instant replacementExpiresAt, Instant now);

    void revokeRefreshToken(String tokenHash, Instant now);

    void revokeAllRefreshTokens(UUID userId, Instant now);

    void saveOAuthExchangeCode(UUID userId, String tokenHash, Instant expiresAt);

    Optional<UUID> consumeOAuthExchangeCode(String tokenHash, Instant now);
}
