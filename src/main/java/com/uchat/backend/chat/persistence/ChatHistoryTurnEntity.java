package com.uchat.backend.chat.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chat_history_turns")
public class ChatHistoryTurnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "principal_name", nullable = false, length = 128)
    private String principalName;

    @Column(name = "conversation_id", nullable = false, length = 128)
    private String conversationId;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatHistoryTurnEntity() {
    }

    public ChatHistoryTurnEntity(String principalName, String conversationId, String role, String content, Instant createdAt) {
        this.principalName = principalName;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}