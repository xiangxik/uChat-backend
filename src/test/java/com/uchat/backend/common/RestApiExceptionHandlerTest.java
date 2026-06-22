package com.uchat.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RestApiExceptionHandlerTest {

    private final RestApiExceptionHandler handler = new RestApiExceptionHandler();

    @Test
    void returnsUnifiedBadRequestForIllegalArgument() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/feedback");
        var response = handler.handleIllegalArgument(new IllegalArgumentException("invalid payload"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("invalid payload");
        assertThat(response.getBody().path()).isEqualTo("/api/feedback");
        assertThat(response.getBody().details()).isEmpty();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void returnsUnifiedInternalErrorForUnexpectedException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/config");
        var response = handler.handleUnexpected(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.INTERNAL_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error.");
        assertThat(response.getBody().path()).isEqualTo("/api/config");
        assertThat(response.getBody().details()).isEmpty();
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
