package com.uchat.backend.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChatContractSerializationTest {

        private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void serializesChatMessageResponseWithExactContractFields() throws Exception {
        ChatMessageResponse payload = new ChatMessageResponse(
                "bot-1",
                "client-1",
                "conv-1",
                "bot",
                "hello",
                Instant.parse("2026-06-21T08:00:00Z")
        );

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(payload));

        assertThat(fieldNamesOf(root))
                .containsExactlyInAnyOrder("id", "clientMessageId", "conversationId", "sender", "content", "timestamp");
        assertThat(root.get("id").asText()).isEqualTo("bot-1");
        assertThat(root.get("clientMessageId").asText()).isEqualTo("client-1");
        assertThat(root.get("conversationId").asText()).isEqualTo("conv-1");
        assertThat(root.get("sender").asText()).isEqualTo("bot");
        assertThat(root.get("content").asText()).isEqualTo("hello");
        assertThat(root.get("timestamp").asText()).isEqualTo("2026-06-21T08:00:00Z");
    }

    @Test
    void serializesChatErrorResponseWithExactContractFields() throws Exception {
        ChatErrorResponse payload = new ChatErrorResponse(
                "CHAT_BAD_REQUEST",
                "locale must be zh or en",
            "client-1",
                Instant.parse("2026-06-21T08:00:00Z")
        );

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(payload));

        assertThat(fieldNamesOf(root)).containsExactlyInAnyOrder("code", "message", "clientMessageId", "timestamp");
        assertThat(root.get("code").asText()).isEqualTo("CHAT_BAD_REQUEST");
        assertThat(root.get("message").asText()).isEqualTo("locale must be zh or en");
        assertThat(root.get("clientMessageId").asText()).isEqualTo("client-1");
        assertThat(root.get("timestamp").asText()).isEqualTo("2026-06-21T08:00:00Z");
    }

    private static Set<String> fieldNamesOf(JsonNode root) {
        Set<String> names = new HashSet<>();
        root.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
