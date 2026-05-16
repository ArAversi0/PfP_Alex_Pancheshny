package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.identityaccess.entity.User;

public record AuthSession(String accessToken, String refreshToken, String tokenType, User user) {

    public AuthSession(String accessToken, String refreshToken, User user) {
        this(accessToken, refreshToken, "Bearer", user);
    }
}
