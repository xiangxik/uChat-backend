package com.uchat.backend.feedback;

import java.time.Instant;

public record FeedbackResponse(
        String status,
        String message,
        Instant timestamp
) {
}
