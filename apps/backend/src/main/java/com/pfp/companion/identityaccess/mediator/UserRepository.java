package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.identityaccess.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByOAuthIdentity(String provider, String providerSubject);

    void deleteById(UUID id);
}

