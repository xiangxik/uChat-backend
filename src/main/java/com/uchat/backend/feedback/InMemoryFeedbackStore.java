package com.uchat.backend.feedback;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "uchat.storage", name = "provider", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryFeedbackStore implements FeedbackStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryFeedbackStore.class);

    private final CopyOnWriteArrayList<FeedbackEntry> entries = new CopyOnWriteArrayList<>();

    @Override
    public void saveFeedback(String messageId, int rating) {
        if (messageId == null || messageId.isBlank()) {
            return;
        }
        entries.add(new FeedbackEntry(messageId.trim(), rating, Instant.now()));
        logger.debug("Stored feedback in memory for message {} with rating {}", messageId, rating);
    }

    List<FeedbackEntry> snapshot() {
        return List.copyOf(new ArrayList<>(entries));
    }

    record FeedbackEntry(String messageId, int rating, Instant createdAt) {
    }
}
