package com.uchat.backend.feedback.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "feedback_entries")
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, length = 128)
    private String messageId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FeedbackEntity() {
    }

    public FeedbackEntity(String messageId, int rating, Instant createdAt) {
        this.messageId = messageId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getRating() {
        return rating;
    }
}
