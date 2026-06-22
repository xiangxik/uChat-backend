package com.uchat.backend.feedback;

public interface FeedbackStore {

    void saveFeedback(String messageId, int rating);
}
