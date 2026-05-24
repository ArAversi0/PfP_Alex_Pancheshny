package com.pfp.companion.content.mediator;

import static org.assertj.core.api.Assertions.assertThat;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContentTreeServiceTest {

    @Test
    void buildsNestedTreeInRepositoryOrder() {
        ContentTreeService service = new ContentTreeService(new InMemoryContentNodeRepository(List.of(
                node("root-a", null, 10),
                node("child-a", "root-a", 20),
                node("root-b", null, 30),
                node("child-b", "root-a", 40))));

        List<ContentTreeService.ContentNodeView> tree = service.tree(ContentSection.RULES);

        assertThat(tree).extracting(ContentTreeService.ContentNodeView::slug)
                .containsExactly("root-a", "root-b");
        assertThat(tree.get(0).children()).extracting(ContentTreeService.ContentNodeView::slug)
                .containsExactly("child-a", "child-b");
    }

    @Test
    void findsNestedNodeWithItsChildren() {
        ContentTreeService service = new ContentTreeService(new InMemoryContentNodeRepository(List.of(
                node("root", null, 10),
                node("child", "root", 20),
                node("grandchild", "child", 30))));

        Optional<ContentTreeService.ContentNodeView> node = service.findNode(ContentSection.RULES, "child");

        assertThat(node).isPresent();
        assertThat(node.orElseThrow().children()).extracting(ContentTreeService.ContentNodeView::slug)
                .containsExactly("grandchild");
    }

    private static ContentNode node(String slug, String parentSlug, int sortOrder) {
        return new ContentNode(sortOrder, ContentSection.RULES, slug, parentSlug, slug, "",
                "Content for " + slug, sortOrder, true, Instant.parse("2026-01-01T00:00:00Z"));
    }

    private record InMemoryContentNodeRepository(List<ContentNode> nodes) implements ContentNodeRepository {

        @Override
        public List<ContentNode> findPublishedBySection(ContentSection section) {
            return nodes;
        }

        @Override
        public Optional<ContentNode> findPublishedBySectionAndSlug(ContentSection section, String slug) {
            return nodes.stream().filter(node -> node.slug().equals(slug)).findFirst();
        }

        @Override
        public List<ContentNode> findAllBySection(ContentSection section) {
            return nodes;
        }

        @Override
        public Optional<ContentNode> findBySectionAndSlug(ContentSection section, String slug) {
            return findPublishedBySectionAndSlug(section, slug);
        }

        @Override
        public ContentNode save(ContentNode node) {
            return node;
        }

        @Override
        public boolean hasChildren(ContentSection section, String parentSlug) {
            return nodes.stream().anyMatch(node -> parentSlug.equals(node.parentSlug()));
        }

        @Override
        public void deleteBySectionAndSlug(ContentSection section, String slug) {
        }
    }
}
