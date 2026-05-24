package com.pfp.companion.identityaccess.mediator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.pfp.companion.content.mediator.ContentNodeRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminUserServiceTest {

    private final AdminUserRepository repository = mock(AdminUserRepository.class);
    private final ContentNodeRepository contentNodeRepository = mock(ContentNodeRepository.class);
    private final AdminUserService service = new AdminUserService(repository, contentNodeRepository);

    @Test
    void blocksDeletingOwnAccount() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> service.deleteUser(adminId, adminId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own account");
    }

    @Test
    void deletesAnotherUser() {
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        service.deleteUser(adminId, userId);

        verify(repository).deleteUser(userId);
    }
}
