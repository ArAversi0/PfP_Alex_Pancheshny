package com.pfp.companion.identityaccess.mediator;

import com.pfp.companion.content.entity.ContentSection;
import com.pfp.companion.content.mediator.ContentNodeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final AdminUserRepository repository;
    private final ContentNodeRepository contentNodeRepository;

    public AdminUserService(AdminUserRepository repository, ContentNodeRepository contentNodeRepository) {
        this.repository = repository;
        this.contentNodeRepository = contentNodeRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> listUsers() {
        return repository.listUsers();
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummary dashboard() {
        long publishedContent = contentNodeRepository.findPublishedBySection(ContentSection.LORE).size()
                + contentNodeRepository.findPublishedBySection(ContentSection.RULES).size();
        return new AdminDashboardSummary(repository.countUsers(), repository.countCharacters(),
                publishedContent);
    }

    @Transactional
    public void deleteUser(UUID requesterId, UUID targetId) {
        if (requesterId.equals(targetId)) {
            throw new IllegalStateException("administrator cannot delete their own account");
        }
        repository.deleteUser(targetId);
    }

    public record AdminDashboardSummary(long users, long characters, long publishedContent) {
    }
}
