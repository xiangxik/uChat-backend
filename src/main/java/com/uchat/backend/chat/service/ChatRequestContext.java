package com.uchat.backend.chat.service;

import java.util.List;

public record ChatRequestContext(
        String conversationId,
        String clientMessageId,
        String principalName,
        String content,
        String locale,
        List<ConversationTurn> history
) {

    public record ConversationTurn(String role, String content) {
    }
}
