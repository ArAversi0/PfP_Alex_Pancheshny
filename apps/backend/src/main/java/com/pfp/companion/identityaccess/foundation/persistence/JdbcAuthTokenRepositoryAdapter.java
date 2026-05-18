package com.pfp.companion.identityaccess.foundation.persistence;

import com.pfp.companion.identityaccess.mediator.AuthTokenRepository;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcAuthTokenRepositoryAdapter implements AuthTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthTokenRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveEmailVerificationToken(UUID userId, String tokenHash, Instant expiresAt) {
        insertToken("email_verification_tokens", userId, tokenHash, expiresAt);
    }

    @Override
    @Transactional
    public Optional<UUID> consumeEmailVerificationToken(String tokenHash, Instant now) {
        return consumeSingleUseToken("email_verification_tokens", tokenHash, now);
    }

    @Override
    public void revokeEmailVerificationTokens(UUID userId, Instant now) {
        revokeUserTokens("email_verification_tokens", userId, now);
    }

    @Override
    public void savePasswordResetToken(UUID userId, String tokenHash, Instant expiresAt) {
        insertToken("password_reset_tokens", userId, tokenHash, expiresAt);
    }

    @Override
    @Transactional
    public Optional<UUID> consumePasswordResetToken(String tokenHash, Instant now) {
        return consumeSingleUseToken("password_reset_tokens", tokenHash, now);
    }

    @Override
    public void revokePasswordResetTokens(UUID userId, Instant now) {
        revokeUserTokens("password_reset_tokens", userId, now);
    }

    @Override
    public void saveRefreshToken(UUID userId, String tokenHash, Instant expiresAt) {
        insertToken("refresh_tokens", userId, tokenHash, expiresAt);
    }

    @Override
    @Transactional
    public Optional<UUID> rotateRefreshToken(String currentTokenHash, String replacementTokenHash,
            Instant replacementExpiresAt, Instant now) {
        Optional<UUID> userId = jdbcTemplate.query("""
                UPDATE refresh_tokens
                SET revoked_at = ?, replaced_by_token_hash = ?
                WHERE token_hash = ?
                  AND revoked_at IS NULL
                  AND replaced_by_token_hash IS NULL
                  AND expires_at > ?
                RETURNING (SELECT public_id FROM users WHERE id = refresh_tokens.user_id)
                """, (resultSet, row) -> resultSet.getObject(1, UUID.class),
                timestamp(now), replacementTokenHash, currentTokenHash, timestamp(now))
                .stream().findFirst();
        userId.ifPresent(id -> saveRefreshToken(id, replacementTokenHash, replacementExpiresAt));
        return userId;
    }

    @Override
    public void revokeRefreshToken(String tokenHash, Instant now) {
        jdbcTemplate.update("""
                UPDATE refresh_tokens
                SET revoked_at = COALESCE(revoked_at, ?)
                WHERE token_hash = ?
                """, timestamp(now), tokenHash);
    }

    @Override
    public void revokeAllRefreshTokens(UUID userId, Instant now) {
        revokeUserTokens("refresh_tokens", userId, now);
    }

    @Override
    public void saveOAuthExchangeCode(UUID userId, String tokenHash, Instant expiresAt) {
        insertToken("oauth_exchange_codes", userId, tokenHash, expiresAt);
    }

    @Override
    @Transactional
    public Optional<UUID> consumeOAuthExchangeCode(String tokenHash, Instant now) {
        return consumeSingleUseToken("oauth_exchange_codes", tokenHash, now);
    }

    private void insertToken(String table, UUID userId, String tokenHash, Instant expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO %s (user_id, token_hash, expires_at)
                SELECT id, ?, ? FROM users WHERE public_id = ?
                """.formatted(table), tokenHash, timestamp(expiresAt), userId);
    }

    private Optional<UUID> consumeSingleUseToken(String table, String tokenHash, Instant now) {
        return jdbcTemplate.query("""
                UPDATE %s
                SET used_at = ?
                WHERE token_hash = ?
                  AND used_at IS NULL
                  AND revoked_at IS NULL
                  AND expires_at > ?
                RETURNING (SELECT public_id FROM users WHERE id = %s.user_id)
                """.formatted(table, table), (resultSet, row) -> resultSet.getObject(1, UUID.class),
                timestamp(now), tokenHash, timestamp(now)).stream().findFirst();
    }

    private void revokeUserTokens(String table, UUID userId, Instant now) {
        jdbcTemplate.update("""
                UPDATE %s
                SET revoked_at = COALESCE(revoked_at, ?)
                WHERE user_id = (SELECT id FROM users WHERE public_id = ?)
                  AND revoked_at IS NULL
                """.formatted(table), timestamp(now), userId);
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }
}
