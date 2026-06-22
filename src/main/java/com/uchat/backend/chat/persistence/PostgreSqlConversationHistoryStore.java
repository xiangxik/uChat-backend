package com.uchat.backend.chat.persistence;

import com.uchat.backend.chat.service.ChatRequestContext;
import com.uchat.backend.chat.service.ConversationHistoryStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "uchat.storage", name = "provider", havingValue = "postgres")
public class PostgreSqlConversationHistoryStore implements ConversationHistoryStore {

    private final ChatHistoryTurnRepository repository;

    public PostgreSqlConversationHistoryStore(ChatHistoryTurnRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRequestContext.ConversationTurn> recentTurns(String principalName, String conversationId, int historyWindow) {
        int effectiveWindow = Math.max(0, historyWindow);
        if (effectiveWindow == 0 || conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        List<ChatHistoryTurnEntity> entities = repository.findByPrincipalNameAndConversationIdOrderByCreatedAtDescIdDesc(
                normalizedPrincipalName(principalName),
                conversationId.trim(),
                PageRequest.of(0, effectiveWindow)
        );

        List<ChatRequestContext.ConversationTurn> turns = new ArrayList<>(entities.size());
        for (int index = entities.size() - 1; index >= 0; index--) {
            ChatHistoryTurnEntity entity = entities.get(index);
            turns.add(new ChatRequestContext.ConversationTurn(entity.getRole(), entity.getContent()));
        }
        return List.copyOf(turns);
    }

    @Override
    @Transactional
    public void appendUserTurn(String principalName, String conversationId, String content) {
        appendTurn(principalName, conversationId, "user", content);
    }

    @Override
    @Transactional
    public void appendBotTurn(String principalName, String conversationId, String content) {
        appendTurn(principalName, conversationId, "assistant", content);
    }

    private void appendTurn(String principalName, String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank() || content == null || content.isBlank()) {
            return;
        }

        repository.save(new ChatHistoryTurnEntity(
                normalizedPrincipalName(principalName),
                conversationId.trim(),
                role,
                content,
                Instant.now()
        ));
    }

    private String normalizedPrincipalName(String principalName) {
        return principalName == null || principalName.isBlank() ? "anonymous" : principalName.trim();
    }
}