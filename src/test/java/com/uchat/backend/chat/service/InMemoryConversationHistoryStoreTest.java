package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchat.backend.config.UChatProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryConversationHistoryStoreTest {

    @Test
    void returnsOnlyMostRecentTurnsWithinWindow() {
        InMemoryConversationHistoryStore store = new InMemoryConversationHistoryStore(properties(2));

        store.appendUserTurn("user-1", "conv-1", "u1");
        store.appendBotTurn("user-1", "conv-1", "b1");
        store.appendUserTurn("user-1", "conv-1", "u2");
        store.appendBotTurn("user-1", "conv-1", "b2");
        store.appendUserTurn("user-1", "conv-1", "u3");

        List<ChatRequestContext.ConversationTurn> turns = store.recentTurns("user-1", "conv-1", 3);

        assertThat(turns).hasSize(3);
        assertThat(turns.get(0).content()).isEqualTo("u2");
        assertThat(turns.get(1).content()).isEqualTo("b2");
        assertThat(turns.get(2).content()).isEqualTo("u3");
    }

    private UChatProperties properties(int contextWindow) {
        return new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors",
                new UChatProperties.LlmProperties(
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
                        contextWindow,
                        "system"
                )
        );
    }
}
