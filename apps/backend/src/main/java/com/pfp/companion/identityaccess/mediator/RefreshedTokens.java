package com.pfp.companion.identityaccess.mediator;

public record RefreshedTokens(String accessToken, String refreshToken, String tokenType) {

    public RefreshedTokens(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
