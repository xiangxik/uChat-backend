package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MockChatReplyServiceTest {

    private final MockChatReplyService service = new MockChatReplyService();

    private ChatRequestContext context(String content, String locale) {
        return new ChatRequestContext(
                "conv-1",
                "client-1",
                "user-1",
                content,
                locale,
                java.util.List.of()
        );
    }

    @Test
    void returnsLocalizedPromptForEmptyContent() {
        assertThat(service.generateReply(context("   ", "en")))
                .isEqualTo("Please type your question first. I will organize the relevant information for you right away.");
        assertThat(service.generateReply(context("", "zh")))
                .isEqualTo("请先输入你的问题，我会即时为你整理相关资讯。");
    }

    @Test
    void returnsMappedReplyWhenKeywordMatches() {
        assertThat(service.generateReply(context("I need a loan for a new car", "en")))
                .contains("personal loans");
        assertThat(service.generateReply(context("我想了解开户流程", "zh")))
                .contains("所需文件清单");
    }

    @Test
    void returnsDefaultReplyWhenNothingMatches() {
        assertThat(service.generateReply(context("something else entirely", "en")))
                .contains("clarifying the service type");
    }

    @Test
    void rejectsUnsupportedLocale() {
        assertThatThrownBy(() -> service.generateReply(context("hello", "fr")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported locale");
    }
}
