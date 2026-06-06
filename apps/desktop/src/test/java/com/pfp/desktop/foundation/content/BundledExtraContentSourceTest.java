package com.pfp.desktop.foundation.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class BundledExtraContentSourceTest {

    private final BundledExtraContentSource source = new BundledExtraContentSource();

    @Test
    void loadsBundledLoreTree() throws IOException {
        assertThat(source.load(ContentSection.LORE))
                .extracting(ContentNode::slug)
                .contains("world", "history", "peoples");
    }

    @Test
    void loadsBundledRulesTreeWithNestedCharacterCreation() throws IOException {
        ContentNode characterCreation = source.load(ContentSection.RULES).stream()
                .filter(node -> node.slug().equals("character-creation"))
                .findFirst()
                .orElseThrow();

        assertThat(characterCreation.children())
                .extracting(ContentNode::slug)
                .contains("origins", "classes", "specializations");
    }
}
