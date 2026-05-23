package com.pfp.companion.content.foundation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "content_nodes")
class ContentNodeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String section;

    @Column(nullable = false)
    String slug;

    @Column(name = "parent_slug")
    String parentSlug;

    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    String summary;

    @Column(name = "content_markdown", nullable = false)
    String contentMarkdown;

    @Column(name = "sort_order", nullable = false)
    int sortOrder;

    @Column(nullable = false)
    boolean published;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
