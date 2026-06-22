package com.uchat.backend.chat.service;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageApplicationService {

    private final ChatReplyService chatReplyService;
    private final ConversationHistoryStore historyStore;
    private final int contextWindow;

    public ChatMessageApplicationService(
            ChatReplyService chatReplyService,
        ConversationHistoryStore historyStore,
            com.uchat.backend.config.UChatProperties properties
    ) {
        this.chatReplyService = chatReplyService;
        this.historyStore = historyStore;
        this.contextWindow = Math.max(0, properties.llm().contextWindow());
    }

    public ChatMessageResponse createBotMessage(ChatSendRequest request, String principalName) {
        String locale = request.locale() == null || request.locale().isBlank() ? "en" : request.locale();
        ChatRequestContext requestContext = new ChatRequestContext(
                request.conversationId(),
                request.clientMessageId(),
                principalName,
                request.content(),
                locale,
                historyStore.recentTurns(principalName, request.conversationId(), contextWindow)
        );
        String reply = chatReplyService.generateReply(requestContext);

            historyStore.appendUserTurn(principalName, request.conversationId(), request.content());
            historyStore.appendBotTurn(principalName, request.conversationId(), reply);

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
