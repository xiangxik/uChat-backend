package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MockChatReplyServiceTest {

    private final MockChatReplyService service = new MockChatReplyService();

    @Test
    void returnsLocalizedPromptForEmptyContent() {
        assertThat(service.generateReply("   ", "en"))
                .isEqualTo("Please type your question first. I will organize the relevant information for you right away.");
        assertThat(service.generateReply("", "zh"))
                .isEqualTo("请先输入你的问题，我会即时为你整理相关资讯。");
    }

    @Test
    void returnsMappedReplyWhenKeywordMatches() {
        assertThat(service.generateReply("I need a loan for a new car", "en"))
                .contains("personal loans");
        assertThat(service.generateReply("我想了解开户流程", "zh"))
                .contains("所需文件清单");
    }

    @Test
    void returnsDefaultReplyWhenNothingMatches() {
        assertThat(service.generateReply("something else entirely", "en"))
                .contains("clarifying the service type");
    }

    @Test
    void rejectsUnsupportedLocale() {
        assertThatThrownBy(() -> service.generateReply("hello", "fr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported locale");
    }
}
