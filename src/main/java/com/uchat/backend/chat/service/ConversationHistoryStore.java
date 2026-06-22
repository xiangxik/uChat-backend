package com.uchat.backend.chat.service;

import java.util.List;

public interface ConversationHistoryStore {

    List<ChatRequestContext.ConversationTurn> recentTurns(String principalName, String conversationId, int historyWindow);

    void appendUserTurn(String principalName, String conversationId, String content);

    void appendBotTurn(String principalName, String conversationId, String content);
}