package com.uchat.backend.chat.service;

import com.uchat.backend.config.UChatProperties;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Component;

@Component
public class InMemoryConversationHistoryStore {

    private final Map<String, Deque<ChatRequestContext.ConversationTurn>> conversationTurns = new ConcurrentHashMap<>();
    private final int maxTurns;

    public InMemoryConversationHistoryStore(UChatProperties properties) {
        this.maxTurns = Math.max(1, Math.min(properties.llm().contextWindow() * 2, 40));
    }

    public List<ChatRequestContext.ConversationTurn> recentTurns(String conversationId, int historyWindow) {
        Deque<ChatRequestContext.ConversationTurn> turns = conversationTurns.get(conversationId);
        if (turns == null || turns.isEmpty()) {
            return List.of();
        }

        List<ChatRequestContext.ConversationTurn> snapshot = new ArrayList<>(turns);
        int effectiveWindow = Math.max(0, historyWindow);
        if (effectiveWindow == 0 || snapshot.size() <= effectiveWindow) {
            return List.copyOf(snapshot);
        }
        return List.copyOf(snapshot.subList(snapshot.size() - effectiveWindow, snapshot.size()));
    }

    public void appendUserTurn(String conversationId, String content) {
        appendTurn(conversationId, "user", content);
    }

    public void appendBotTurn(String conversationId, String content) {
        appendTurn(conversationId, "assistant", content);
    }

    private void appendTurn(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank() || content == null || content.isBlank()) {
            return;
        }

        Deque<ChatRequestContext.ConversationTurn> turns =
                conversationTurns.computeIfAbsent(conversationId, key -> new ConcurrentLinkedDeque<>());
        turns.addLast(new ChatRequestContext.ConversationTurn(role, content));

        while (turns.size() > maxTurns) {
            turns.pollFirst();
        }
    }
}
