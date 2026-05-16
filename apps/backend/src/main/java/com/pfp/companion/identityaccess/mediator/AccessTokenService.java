package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.identityaccess.entity.User;

public interface AccessTokenService {

    String generate(User user);
}
