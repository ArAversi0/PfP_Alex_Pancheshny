package com.pfp.desktop.foundation.api;

public record AuthSession(String accessToken, String refreshToken, String tokenType, AuthUser user) {
    public String authorizationHeader() {
        String type = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;
        return type + " " + accessToken;
    }
}
