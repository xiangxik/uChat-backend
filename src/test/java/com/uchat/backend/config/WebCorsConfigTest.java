package com.uchat.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class WebCorsConfigTest {

    @Test
    void configuresApiAndActuatorCorsMappings() throws Exception {
        TestCorsRegistry registry = new TestCorsRegistry();
        WebCorsConfig config = new WebCorsConfig(new UChatProperties(
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
        ));

        config.addCorsMappings(registry);

        Map<String, CorsConfiguration> configurations = registry.configurations();
        assertThat(configurations).containsKeys("/api/**", "/actuator/**");
        assertThat(configurations.get("/api/**").getAllowedOrigins())
                .containsExactly("http://localhost:5173", "http://127.0.0.1:5173");
        assertThat(configurations.get("/api/**").getAllowedMethods())
                .containsExactly("GET", "POST", "OPTIONS");
        assertThat(configurations.get("/actuator/**").getAllowedMethods())
                .containsExactly("GET", "OPTIONS");
    }

    private static class TestCorsRegistry extends CorsRegistry {

        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
