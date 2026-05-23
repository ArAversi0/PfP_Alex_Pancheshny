package com.pfp.companion.content.foundation.persistence;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import com.pfp.companion.content.mediator.ContentNodeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaContentNodeRepositoryAdapter implements ContentNodeRepository {

    private final ContentNodeJpaRepository repository;

    JpaContentNodeRepositoryAdapter(ContentNodeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ContentNode> findPublishedBySection(ContentSection section) {
        return repository.findAllBySectionAndPublishedTrueOrderBySortOrderAscSlugAsc(section.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ContentNode> findPublishedBySectionAndSlug(ContentSection section, String slug) {
        return repository.findBySectionAndSlugAndPublishedTrue(section.name(), slug).map(this::toDomain);
    }

    @Override
    public List<ContentNode> findAllBySection(ContentSection section) {
        return repository.findAllBySectionOrderBySortOrderAscSlugAsc(section.name())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ContentNode> findBySectionAndSlug(ContentSection section, String slug) {
        return repository.findBySectionAndSlug(section.name(), slug).map(this::toDomain);
    }

    @Override
    @Transactional
    public ContentNode save(ContentNode node) {
        ContentNodeJpaEntity entity = node.id() == 0
                ? repository.findBySectionAndSlug(node.section().name(), node.slug())
                        .orElseGet(ContentNodeJpaEntity::new)
                : repository.findById(node.id()).orElseGet(ContentNodeJpaEntity::new);
        entity.section = node.section().name();
        entity.slug = node.slug();
        entity.parentSlug = node.parentSlug();
        entity.title = node.title();
        entity.summary = node.summary();
        entity.contentMarkdown = node.contentMarkdown();
        entity.sortOrder = node.sortOrder();
        entity.published = node.published();
        entity.updatedAt = node.updatedAt();
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public boolean hasChildren(ContentSection section, String parentSlug) {
        return repository.existsBySectionAndParentSlug(section.name(), parentSlug);
    }

    @Override
    @Transactional
    public void deleteBySectionAndSlug(ContentSection section, String slug) {
        repository.deleteBySectionAndSlug(section.name(), slug);
        repository.flush();
    }

    private ContentNode toDomain(ContentNodeJpaEntity entity) {
        return new ContentNode(entity.id, ContentSection.valueOf(entity.section), entity.slug, entity.parentSlug,
                entity.title, entity.summary, entity.contentMarkdown, entity.sortOrder, entity.published,
                entity.updatedAt);
    }
}
