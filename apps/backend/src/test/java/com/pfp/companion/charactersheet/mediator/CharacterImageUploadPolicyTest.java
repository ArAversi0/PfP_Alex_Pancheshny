package com.pfp.companion.charactersheet.mediator;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CharacterImageUploadPolicyTest {

    private final CharacterImageUploadPolicy policy = new CharacterImageUploadPolicy();

    @Test
    void acceptsJpgUpToTwoMegabytes() {
        assertThatNoException().isThrownBy(
                () -> policy.validate("portrait.jpg", "image/jpeg", 2L * 1024 * 1024));
    }

    @Test
    void rejectsOtherFormatsAndOversizedFiles() {
        assertThatThrownBy(() -> policy.validate("portrait.png", "image/png", 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.validate("portrait.jpg", "image/jpeg", 2L * 1024 * 1024 + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

