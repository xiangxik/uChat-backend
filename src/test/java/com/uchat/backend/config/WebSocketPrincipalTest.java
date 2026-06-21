package com.uchat.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import org.junit.jupiter.api.Test;

class WebSocketPrincipalTest {

    @Test
    void exposesRecordNameThroughPrincipalContract() {
        WebSocketPrincipal principal = new WebSocketPrincipal("user-123");

        assertThat(principal).isInstanceOf(Principal.class);
        assertThat(principal.name()).isEqualTo("user-123");
        assertThat(principal.getName()).isEqualTo("user-123");
    }
}
