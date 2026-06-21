package com.uchat.backend.chat.dto;

import java.time.Instant;

public record ChatMessageResponse(
        String id,
        String clientMessageId,
        String conversationId,
        String sender,
        String content,
        Instant timestamp
) {
}
