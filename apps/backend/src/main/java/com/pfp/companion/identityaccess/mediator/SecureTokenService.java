package com.pfp.companion.identityaccess.mediator;

public interface SecureTokenService {

    String generate();

    String hash(String rawToken);
}
