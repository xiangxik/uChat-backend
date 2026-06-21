package com.uchat.backend.chat.dto;

import java.time.Instant;

public record ChatErrorResponse(
        String code,
        String message,
        String clientMessageId,
        Instant timestamp
) {

        public ChatErrorResponse {
                if (clientMessageId == null || clientMessageId.isBlank()) {
                        throw new IllegalArgumentException("clientMessageId must not be blank");
                }
        }
}
