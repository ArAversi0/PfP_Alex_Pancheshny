package com.pfp.companion.content.mediator;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminContentService {

    private final ContentNodeRepository repository;
    private final Clock clock;

    public AdminContentService(ContentNodeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ContentNode> list(ContentSection section) {
        return repository.findAllBySection(section);
    }

    @Transactional
    public ContentNode create(ContentNodeDraft draft) {
        if (repository.findBySectionAndSlug(draft.section(), draft.slug()).isPresent()) {
            throw new IllegalStateException("content slug is already used");
        }
        validateParent(draft.section(), draft.slug(), draft.parentSlug());
        return repository.save(new ContentNode(0, draft.section(), draft.slug(), draft.parentSlug(),
                draft.title(), draft.summary(), draft.contentMarkdown(), draft.sortOrder(),
                draft.published(), clock.instant()));
    }

    @Transactional
    public ContentNode update(ContentSection section, String slug, ContentNodeDraft draft) {
        ContentNode current = repository.findBySectionAndSlug(section, slug)
                .orElseThrow(() -> new IllegalArgumentException("content node is unavailable"));
        validateParent(section, slug, draft.parentSlug());
        return repository.save(new ContentNode(current.id(), section, slug, draft.parentSlug(),
                draft.title(), draft.summary(), draft.contentMarkdown(), draft.sortOrder(),
                draft.published(), clock.instant()));
    }

    @Transactional
    public void delete(ContentSection section, String slug) {
        if (repository.findBySectionAndSlug(section, slug).isEmpty()) {
            throw new IllegalArgumentException("content node is unavailable");
        }
        if (repository.hasChildren(section, slug)) {
            throw new IllegalStateException("content node has child articles");
        }
        repository.deleteBySectionAndSlug(section, slug);
    }

    private void validateParent(ContentSection section, String slug, String parentSlug) {
        if (parentSlug == null || parentSlug.isBlank()) {
            return;
        }
        if (slug.equals(parentSlug)) {
            throw new IllegalArgumentException("content node cannot be its own parent");
        }
        if (repository.findBySectionAndSlug(section, parentSlug).isEmpty()) {
            throw new IllegalArgumentException("parent content node is unavailable");
        }
    }

    public record ContentNodeDraft(ContentSection section, String slug, String parentSlug,
            String title, String summary, String contentMarkdown, int sortOrder, boolean published) {
    }
}
