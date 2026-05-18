package com.pfp.companion.identityaccess.foundation.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pfp.auth")
public class AuthProperties {

    private String jwtSecret = "";
    private Duration accessTokenLifetime = Duration.ofMinutes(15);
    private Duration refreshTokenLifetime = Duration.ofDays(30);
    private Duration verificationTokenLifetime = Duration.ofHours(24);
    private Duration passwordResetTokenLifetime = Duration.ofHours(1);
    private Duration oauth2ExchangeCodeLifetime = Duration.ofMinutes(2);
    private String frontendBaseUrl = "http://localhost:5173";
    private String oauth2FrontendCallbackUrl = "http://localhost:5173/oauth2/callback";
    private String googleClientId = "";
    private String googleClientSecret = "";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Duration getAccessTokenLifetime() {
        return accessTokenLifetime;
    }

    public void setAccessTokenLifetime(Duration accessTokenLifetime) {
        this.accessTokenLifetime = accessTokenLifetime;
    }

    public Duration getRefreshTokenLifetime() {
        return refreshTokenLifetime;
    }

    public void setRefreshTokenLifetime(Duration refreshTokenLifetime) {
        this.refreshTokenLifetime = refreshTokenLifetime;
    }

    public Duration getVerificationTokenLifetime() {
        return verificationTokenLifetime;
    }

    public void setVerificationTokenLifetime(Duration verificationTokenLifetime) {
        this.verificationTokenLifetime = verificationTokenLifetime;
    }

    public Duration getPasswordResetTokenLifetime() {
        return passwordResetTokenLifetime;
    }

    public void setPasswordResetTokenLifetime(Duration passwordResetTokenLifetime) {
        this.passwordResetTokenLifetime = passwordResetTokenLifetime;
    }

    public Duration getOauth2ExchangeCodeLifetime() {
        return oauth2ExchangeCodeLifetime;
    }

    public void setOauth2ExchangeCodeLifetime(Duration oauth2ExchangeCodeLifetime) {
        this.oauth2ExchangeCodeLifetime = oauth2ExchangeCodeLifetime;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String getOauth2FrontendCallbackUrl() {
        return oauth2FrontendCallbackUrl;
    }

    public void setOauth2FrontendCallbackUrl(String oauth2FrontendCallbackUrl) {
        this.oauth2FrontendCallbackUrl = oauth2FrontendCallbackUrl;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }
}
