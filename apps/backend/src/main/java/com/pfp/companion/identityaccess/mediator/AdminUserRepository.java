package com.pfp.companion.identityaccess.mediator;

import java.util.List;
import java.util.UUID;

public interface AdminUserRepository {

    List<AdminUserSummary> listUsers();

    long countUsers();

    long countCharacters();

    void deleteUser(UUID userId);
}
