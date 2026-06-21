package com.uchat.backend.chat.service;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageApplicationService {

    private final ChatReplyService chatReplyService;

    public ChatMessageApplicationService(ChatReplyService chatReplyService) {
        this.chatReplyService = chatReplyService;
    }

    public ChatMessageResponse createBotMessage(ChatSendRequest request) {
        String locale = request.locale() == null || request.locale().isBlank() ? "en" : request.locale();
        String reply = chatReplyService.generateReply(request.content(), locale);

        return new ChatMessageResponse(
                UUID.randomUUID().toString(),
                request.clientMessageId(),
                request.conversationId(),
                "bot",
                reply,
                Instant.now()
        );
    }
}
