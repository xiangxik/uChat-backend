package com.uchat.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
class RestApiExceptionHandlerTest {

    private final RestApiExceptionHandler handler = new RestApiExceptionHandler();

    @Test
    void returnsUnifiedBadRequestForIllegalArgument() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("invalid payload"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("invalid payload");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void returnsUnifiedInternalErrorForUnexpectedException() {
        var response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error.");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
