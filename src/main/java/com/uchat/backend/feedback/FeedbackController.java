package com.uchat.backend.feedback;

import jakarta.validation.Valid;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackStore feedbackStore;

    public FeedbackController(FeedbackStore feedbackStore) {
        this.feedbackStore = feedbackStore;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FeedbackResponse submit(@Valid @RequestBody FeedbackRequest request) {
        feedbackStore.saveFeedback(request.messageId(), request.rating());
        logger.info("Received feedback for message {} with rating {}", request.messageId(), request.rating());
        return new FeedbackResponse("accepted", "Feedback received.", Instant.now());
    }
}
