package com.pfp.companion.bootstrap;

import com.pfp.companion.identityaccess.foundation.security.AuthProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
public class GoogleOAuth2ClientConfiguration {

    private final AuthProperties properties;

    public GoogleOAuth2ClientConfiguration(AuthProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validateConfiguration() {
        boolean hasClientId = StringUtils.hasText(properties.getGoogleClientId());
        boolean hasClientSecret = StringUtils.hasText(properties.getGoogleClientSecret());
        if (hasClientId != hasClientSecret) {
            throw new IllegalStateException(
                    "PFP_GOOGLE_CLIENT_ID and PFP_GOOGLE_CLIENT_SECRET must be configured together");
        }
    }

    @Bean
    @Conditional(GoogleOAuth2ConfiguredCondition.class)
    ClientRegistrationRepository googleClientRegistrationRepository() {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(properties.getGoogleClientId())
                .clientSecret(properties.getGoogleClientSecret())
                .scope("openid", "profile", "email")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    static final class GoogleOAuth2ConfiguredCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(context.getEnvironment().getProperty("pfp.auth.google-client-id"))
                    && StringUtils.hasText(
                            context.getEnvironment().getProperty("pfp.auth.google-client-secret"));
        }
    }
}
