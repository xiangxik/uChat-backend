package com.uchat.backend.chat;

import com.uchat.backend.chat.dto.ChatErrorResponse;
import java.security.Principal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.validation.FieldError;

@ControllerAdvice
public class ChatMessagingExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessagingExceptionHandler.class);

    private final ChatUserMessagingGateway chatUserMessagingGateway;

    public ChatMessagingExceptionHandler(ChatUserMessagingGateway chatUserMessagingGateway) {
        this.chatUserMessagingGateway = chatUserMessagingGateway;
    }

    @MessageExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
        public void handleBadRequest(
            Exception exception,
            Principal principal,
            @Header(name = "clientMessageId", required = false) String clientMessageId,
            @Header(name = "simpSessionId", required = false) String sessionId
        ) {
        String message = extractMessage(exception);
        String principalName = principal != null ? principal.getName() : "anonymous";
        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);
        logger.warn("Chat request rejected for principal {}: {}",
                principalName, message);
        ChatErrorResponse payload = new ChatErrorResponse("CHAT_BAD_REQUEST", message, normalizedClientMessageId, Instant.now());
        chatUserMessagingGateway.sendChatError(principalName, payload, sessionId);
    }

    @MessageExceptionHandler(Exception.class)
        public void handleUnexpected(
            Exception exception,
            Principal principal,
            @Header(name = "clientMessageId", required = false) String clientMessageId,
            @Header(name = "simpSessionId", required = false) String sessionId
        ) {
        String principalName = principal != null ? principal.getName() : "anonymous";
        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);
        logger.error("Unexpected chat failure for principal {}",
                principalName, exception);
        chatUserMessagingGateway.sendChatError(
            principalName,
            new ChatErrorResponse("CHAT_INTERNAL_ERROR", "Unable to process chat request.", normalizedClientMessageId, Instant.now()),
            sessionId
        );
    }

    private String normalizeClientMessageId(String clientMessageId) {
        if (clientMessageId == null || clientMessageId.isBlank()) {
            return "missing-client-message-id";
        }
        return clientMessageId;
    }

    private String extractMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException validationException) {
            return Optional.ofNullable(validationException.getBindingResult().getFieldError())
                    .map(FieldError::getDefaultMessage)
                    .orElse("Invalid chat request.");
        }
        return exception.getMessage() == null ? "Invalid chat request." : exception.getMessage();
    }
}
