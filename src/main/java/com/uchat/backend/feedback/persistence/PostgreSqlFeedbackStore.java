package com.uchat.backend.feedback.persistence;

import com.uchat.backend.feedback.FeedbackStore;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "uchat.storage", name = "provider", havingValue = "postgres")
public class PostgreSqlFeedbackStore implements FeedbackStore {

    private final FeedbackRepository feedbackRepository;

    public PostgreSqlFeedbackStore(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    @Transactional
    public void saveFeedback(String messageId, int rating) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        feedbackRepository.save(new FeedbackEntity(messageId.trim(), rating, Instant.now()));
    }
}
