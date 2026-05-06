package com.pfp.companion.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PfpCompanionApplicationTests {

    @Test
    void bootstrapClassExists() {
        assertThat(PfpCompanionApplication.class).isNotNull();
    }
}

