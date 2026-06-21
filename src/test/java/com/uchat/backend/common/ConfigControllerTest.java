package com.uchat.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchat.backend.config.UChatProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigControllerTest {

    @Test
    void returnsFrontendConfig() {
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
        ConfigController controller = new ConfigController(properties);

        AppConfigResponse response = controller.getConfig();

        assertThat(response.appName()).isEqualTo("uChat");
        assertThat(response.defaultLocale()).isEqualTo("en");
        assertThat(response.webSocketEndpoint()).isEqualTo("/ws");
        assertThat(response.chatSendDestination()).isEqualTo("/app/chat.send");
        assertThat(response.chatMessageSubscription()).isEqualTo("/user/queue/chat.messages");
        assertThat(response.chatErrorSubscription()).isEqualTo("/user/queue/chat.errors");
    }
}
