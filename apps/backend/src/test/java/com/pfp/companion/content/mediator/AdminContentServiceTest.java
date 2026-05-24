package com.pfp.companion.content.mediator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AdminContentServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-04T00:00:00Z");

    private final ContentNodeRepository repository = mock(ContentNodeRepository.class);
    private final AdminContentService service = new AdminContentService(repository,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void blocksDeletingNodeWithChildren() {
        when(repository.findBySectionAndSlug(ContentSection.RULES, "combat"))
                .thenReturn(Optional.of(node("combat")));
        when(repository.hasChildren(ContentSection.RULES, "combat")).thenReturn(true);

        assertThatThrownBy(() -> service.delete(ContentSection.RULES, "combat"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("child");
    }

    @Test
    void deletesLeafNode() {
        when(repository.findBySectionAndSlug(ContentSection.RULES, "initiative"))
                .thenReturn(Optional.of(node("initiative")));

        service.delete(ContentSection.RULES, "initiative");

        verify(repository).deleteBySectionAndSlug(ContentSection.RULES, "initiative");
    }

    private static ContentNode node(String slug) {
        return new ContentNode(1, ContentSection.RULES, slug, null, slug, "", "",
                10, true, NOW);
    }
}
