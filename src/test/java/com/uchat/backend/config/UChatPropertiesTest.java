package com.uchat.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class UChatPropertiesTest {

    @Test
    void exposesConfiguredChatDestinations() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
            List.of("http://localhost:5173", "http://127.0.0.1:5173"),
            "/app",
            "/user",
            List.of("/topic", "/queue"),
            "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors"
        );

        assertThat(properties.appName()).isEqualTo("uChat");
        assertThat(properties.defaultLocale()).isEqualTo("en");
        assertThat(properties.allowedOrigins())
            .containsExactly("http://localhost:5173", "http://127.0.0.1:5173");
        assertThat(properties.applicationDestinationPrefix()).isEqualTo("/app");
        assertThat(properties.userDestinationPrefix()).isEqualTo("/user");
        assertThat(properties.brokerDestinations()).containsExactly("/topic", "/queue");
        assertThat(properties.chatSendMapping()).isEqualTo("/chat.send");
        assertThat(properties.websocketEndpoint()).isEqualTo("/ws");
        assertThat(properties.chatSendDestination()).isEqualTo("/app/chat.send");
        assertThat(properties.chatMessageSubscription()).isEqualTo("/user/queue/chat.messages");
        assertThat(properties.chatErrorSubscription()).isEqualTo("/user/queue/chat.errors");
    }
}
