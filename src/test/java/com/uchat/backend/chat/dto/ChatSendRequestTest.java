package com.uchat.backend.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChatSendRequestTest {

    private final Validator validator = createValidator();

    @Test
    void acceptsSupportedLocalesAndContentWithinLimit() {
        ChatSendRequest request = new ChatSendRequest("conv-1", "client-1", "hello", "zh");

        Set<ConstraintViolation<ChatSendRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsBlankIdsUnsupportedLocaleAndOversizedContent() {
        ChatSendRequest request = new ChatSendRequest("", " ", "x".repeat(4001), "fr");

        Set<ConstraintViolation<ChatSendRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("conversationId", "clientMessageId", "content", "locale");
    }

    private static Validator createValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
