package com.pfp.companion.identityaccess.foundation.persistence;

import com.pfp.companion.identityaccess.entity.Role;
import com.pfp.companion.identityaccess.mediator.AdminUserRepository;
import com.pfp.companion.identityaccess.mediator.AdminUserSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaAdminUserRepositoryAdapter implements AdminUserRepository {

    private final UserJpaRepository repository;

    JpaAdminUserRepositoryAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserSummary> listUsers() {
        return repository.findAdminUserSummaries().stream()
                .map(user -> new AdminUserSummary(user.getPublicId(), user.getEmail(),
                        Role.valueOf(user.getRole()), user.getEmailVerified(), user.getCreatedAt(),
                        user.getCharacterCount()))
                .toList();
    }

    @Override
    public long countUsers() {
        return repository.count();
    }

    @Override
    public long countCharacters() {
        return repository.countCharacters();
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        repository.deleteDirectByPublicId(userId);
        repository.flush();
    }
}
