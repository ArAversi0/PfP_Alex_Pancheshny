package com.pfp.companion.content.control;

import com.pfp.companion.content.entity.ContentSection;
import com.pfp.companion.content.mediator.ContentTreeService;
import com.pfp.companion.content.mediator.ContentTreeService.ContentNodeView;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final ContentTreeService contentTreeService;

    public ContentController(ContentTreeService contentTreeService) {
        this.contentTreeService = contentTreeService;
    }

    @GetMapping("/{section}/nodes")
    public List<ContentNodeResponse> tree(@PathVariable String section) {
        return contentTreeService.tree(parseSection(section)).stream().map(ContentNodeResponse::from).toList();
    }

    @GetMapping("/{section}/nodes/{slug}")
    public ContentNodeResponse node(@PathVariable String section, @PathVariable String slug) {
        return contentTreeService.findNode(parseSection(section), slug)
                .map(ContentNodeResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content article not found"));
    }

    private ContentSection parseSection(String section) {
        try {
            return ContentSection.fromApiPath(section);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content section not found");
        }
    }

    public record ContentNodeResponse(String slug, String parentSlug, String title, String summary,
            String contentMarkdown, int sortOrder, List<ContentNodeResponse> children) {

        static ContentNodeResponse from(ContentNodeView node) {
            return new ContentNodeResponse(node.slug(), node.parentSlug(), node.title(), node.summary(),
                    node.contentMarkdown(), node.sortOrder(),
                    node.children().stream().map(ContentNodeResponse::from).toList());
        }
    }
}
