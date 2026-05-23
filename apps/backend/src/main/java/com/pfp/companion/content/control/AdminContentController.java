package com.pfp.companion.content.control;

import com.pfp.companion.content.entity.ContentNode;
import com.pfp.companion.content.entity.ContentSection;
import com.pfp.companion.content.mediator.AdminContentService;
import com.pfp.companion.content.mediator.AdminContentService.ContentNodeDraft;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/content/nodes")
public class AdminContentController {

    private final AdminContentService service;

    public AdminContentController(AdminContentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminContentNodeResponse> list(@RequestParam ContentSection section) {
        return service.list(section).stream().map(AdminContentNodeResponse::from).toList();
    }

    @PostMapping
    public AdminContentNodeResponse create(@Valid @RequestBody AdminContentNodeRequest request) {
        return AdminContentNodeResponse.from(service.create(request.toDraft()));
    }

    @PutMapping("/{section}/{slug}")
    public AdminContentNodeResponse update(@PathVariable ContentSection section, @PathVariable String slug,
            @Valid @RequestBody AdminContentNodeRequest request) {
        return AdminContentNodeResponse.from(service.update(section, slug, request.toDraft()));
    }

    @DeleteMapping("/{section}/{slug}")
    public DeletedResponse delete(@PathVariable ContentSection section, @PathVariable String slug) {
        service.delete(section, slug);
        return new DeletedResponse(true);
    }

    public record AdminContentNodeRequest(@NotNull ContentSection section,
            @NotBlank @Size(max = 180) String slug,
            @Size(max = 180) String parentSlug,
            @NotBlank @Size(max = 200) String title,
            String summary,
            String contentMarkdown,
            int sortOrder,
            boolean published) {

        ContentNodeDraft toDraft() {
            return new ContentNodeDraft(section, slug, blankToNull(parentSlug), title,
                    summary == null ? "" : summary, contentMarkdown == null ? "" : contentMarkdown,
                    sortOrder, published);
        }

        private static String blankToNull(String value) {
            return value == null || value.isBlank() ? null : value;
        }
    }

    public record AdminContentNodeResponse(String section, String slug, String parentSlug,
            String title, String summary, String contentMarkdown, int sortOrder, boolean published) {

        static AdminContentNodeResponse from(ContentNode node) {
            return new AdminContentNodeResponse(node.section().name(), node.slug(), node.parentSlug(),
                    node.title(), node.summary(), node.contentMarkdown(), node.sortOrder(), node.published());
        }
    }

    public record DeletedResponse(boolean deleted) {
    }
}
