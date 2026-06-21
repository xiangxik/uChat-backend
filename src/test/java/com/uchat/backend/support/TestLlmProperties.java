package com.uchat.backend.support;

import com.uchat.backend.config.UChatProperties;

public final class TestLlmProperties {

    private TestLlmProperties() {
    }

    public static UChatProperties.LlmProperties enabled() {
        return new UChatProperties.LlmProperties(
                true,
                "openai",
                "https://api.openai.com",
                "test-key",
                "gpt-4.1-mini",
                0.4,
                1000,
                20000,
                1,
                300,
                10,
                "system"
        );
    }

    public static UChatProperties.LlmProperties disabled() {
        return new UChatProperties.LlmProperties(
                false,
                "openai",
                "https://api.openai.com",
                "",
                "gpt-4.1-mini",
                0.4,
                1000,
                20000,
                1,
                300,
                10,
                "system"
        );
    }
}
