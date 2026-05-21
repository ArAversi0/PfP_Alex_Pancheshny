package com.pfp.companion.content.mediator;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentTreeService {

    private final ContentNodeRepository repository;

    public ContentTreeService(ContentNodeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ContentNodeView> tree(ContentSection section) {
        return buildTree(repository.findPublishedBySection(section));
    }

    @Transactional(readOnly = true)
    public Optional<ContentNodeView> findNode(ContentSection section, String slug) {
        Optional<ContentNode> node = repository.findPublishedBySectionAndSlug(section, slug);
        if (node.isEmpty()) {
            return Optional.empty();
        }
        List<ContentNodeView> tree = buildTree(repository.findPublishedBySection(section));
        return findInTree(tree, slug);
    }

    private List<ContentNodeView> buildTree(List<ContentNode> nodes) {
        Map<String, MutableContentNodeView> bySlug = new LinkedHashMap<>();
        for (ContentNode node : nodes) {
            bySlug.put(node.slug(), new MutableContentNodeView(node));
        }

        List<MutableContentNodeView> roots = new ArrayList<>();
        for (MutableContentNodeView view : bySlug.values()) {
            String parentSlug = view.node.parentSlug();
            MutableContentNodeView parent = parentSlug == null ? null : bySlug.get(parentSlug);
            if (parent == null) {
                roots.add(view);
            } else {
                parent.children.add(view);
            }
        }
        return roots.stream().map(MutableContentNodeView::freeze).toList();
    }

    private Optional<ContentNodeView> findInTree(List<ContentNodeView> tree, String slug) {
        for (ContentNodeView node : tree) {
            if (node.slug().equals(slug)) {
                return Optional.of(node);
            }
            Optional<ContentNodeView> child = findInTree(node.children(), slug);
            if (child.isPresent()) {
                return child;
            }
        }
        return Optional.empty();
    }

    public record ContentNodeView(String slug, String parentSlug, String title, String summary,
            String contentMarkdown, int sortOrder, List<ContentNodeView> children) {
    }

    private static final class MutableContentNodeView {
        private final ContentNode node;
        private final List<MutableContentNodeView> children = new ArrayList<>();

        private MutableContentNodeView(ContentNode node) {
            this.node = node;
        }

        private ContentNodeView freeze() {
            return new ContentNodeView(node.slug(), node.parentSlug(), node.title(), node.summary(),
                    node.contentMarkdown(), node.sortOrder(),
                    children.stream().map(MutableContentNodeView::freeze).toList());
        }
    }
}
