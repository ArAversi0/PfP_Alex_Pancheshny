package com.pfp.companion.content.mediator;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import java.util.List;
import java.util.Optional;

public interface ContentNodeRepository {

    List<ContentNode> findPublishedBySection(ContentSection section);

    Optional<ContentNode> findPublishedBySectionAndSlug(ContentSection section, String slug);

    List<ContentNode> findAllBySection(ContentSection section);

    Optional<ContentNode> findBySectionAndSlug(ContentSection section, String slug);

    ContentNode save(ContentNode node);

    boolean hasChildren(ContentSection section, String parentSlug);

    void deleteBySectionAndSlug(ContentSection section, String slug);
}
