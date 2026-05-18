package com.pfp.companion.identityaccess.foundation.security;

import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.mediator.AccessTokenService;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public final class JwtAccessTokenService implements AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties properties;
    private final Clock clock;

    public JwtAccessTokenService(JwtEncoder jwtEncoder, AuthProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String generate(User user) {
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("pfp-companion")
                .subject(user.id().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(properties.getAccessTokenLifetime()))
                .claim("email", user.email())
                .claim("role", user.role().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
