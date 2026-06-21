package com.uchat.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void returnsUpStatus() {
        HealthController controller = new HealthController();

        Map<String, Object> response = controller.health();

        assertThat(response.get("status")).isEqualTo("UP");
        assertThat(response.get("service")).isEqualTo("uchat-backend");
        assertThat(response.get("timestamp")).isNotNull();
    }
}
