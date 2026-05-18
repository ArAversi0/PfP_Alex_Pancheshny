package com.pfp.companion.identityaccess.foundation.persistence;

import com.pfp.companion.identityaccess.entity.OAuthIdentity;
import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.entity.User;
import com.pfp.companion.identityaccess.mediator.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userRepository;
    private final OAuthIdentityJpaRepository oauthIdentityRepository;

    public JpaUserRepositoryAdapter(UserJpaRepository userRepository,
            OAuthIdentityJpaRepository oauthIdentityRepository) {
        this.userRepository = userRepository;
        this.oauthIdentityRepository = oauthIdentityRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity target = userRepository.findByPublicId(user.id()).orElseGet(UserJpaEntity::new);
        target.publicId = user.id();
        target.email = user.email();
        target.passwordHash = user.passwordHash();
        target.role = user.role().name();
        target.emailVerified = user.emailVerified();
        target.createdAt = user.createdAt();
        target.oauthIdentities.removeIf(identity -> !user.oauthIdentities().contains(
                new OAuthIdentity(identity.provider, identity.providerSubject)));
        user.oauthIdentities().forEach(identity -> {
            boolean alreadyStored = target.oauthIdentities.stream().anyMatch(stored ->
                    stored.provider.equals(identity.provider())
                            && stored.providerSubject.equals(identity.providerSubject()));
            if (!alreadyStored) {
                OAuthIdentityJpaEntity oauth = new OAuthIdentityJpaEntity();
                oauth.user = target;
                oauth.provider = identity.provider();
                oauth.providerSubject = identity.providerSubject();
                target.oauthIdentities.add(oauth);
            }
        });
        return toDomain(userRepository.saveAndFlush(target));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findByPublicId(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByOAuthIdentity(String provider, String providerSubject) {
        return oauthIdentityRepository.findByProviderAndProviderSubject(provider, providerSubject)
                .map(identity -> toDomain(identity.user));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (userRepository.deleteDirectByPublicId(id) > 0) {
            userRepository.flush();
        }
    }

    private User toDomain(UserJpaEntity source) {
        return new User(source.publicId, source.email, source.passwordHash, Role.valueOf(source.role),
                source.emailVerified, source.createdAt, source.oauthIdentities.stream()
                        .map(identity -> new OAuthIdentity(identity.provider, identity.providerSubject))
                        .toList());
    }
}
