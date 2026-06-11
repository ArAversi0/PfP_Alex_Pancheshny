package com.pfp.companion.identityaccess.foundation.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

class JwtAccessTokenServiceTest {

    @Test
    void generatesSignedAccessTokenWithMinimalAuthorizationClaims() {
        Instant now = Instant.parse("2026-06-02T10:00:00Z");
        SecretKey key = new SecretKeySpec("01234567890123456789012345678901".getBytes(),
                "HmacSHA256");
        AuthProperties properties = new AuthProperties();
        JwtAccessTokenService service = new JwtAccessTokenService(
                new NimbusJwtEncoder(new ImmutableSecret<>(key)), properties,
                Clock.fixed(now, ZoneOffset.UTC));
        User user = new User(UUID.randomUUID(), "user@example.com", "password-hash",
                Role.ROLE_USER, true, now, List.of());

        String rawToken = service.generate(user);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(token -> OAuth2TokenValidatorResult.success());
        Jwt token = decoder.decode(rawToken);

        assertThat(token.getSubject()).isEqualTo(user.id().toString());
        assertThat(token.getClaimAsString("email")).isEqualTo(user.email());
        assertThat(token.getClaimAsString("role")).isEqualTo("ROLE_USER");
        assertThat(token.getExpiresAt()).isEqualTo(now.plusSeconds(15 * 60));
    }
}
