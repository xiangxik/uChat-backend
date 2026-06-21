package com.uchat.backend.chat;

import com.uchat.backend.chat.dto.ChatErrorResponse;
import com.uchat.backend.chat.service.LlmServiceException;
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

        @MessageExceptionHandler(LlmServiceException.class)
        public void handleLlmFailure(
            LlmServiceException exception,
            Principal principal,
            @Header(name = "clientMessageId", required = false) String clientMessageId,
            @Header(name = "simpSessionId", required = false) String sessionId
        ) {
        String principalName = principal != null ? principal.getName() : "anonymous";
        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);
        logger.warn("LLM chat failure for principal {} with code {}", principalName, exception.code());
        chatUserMessagingGateway.sendChatError(
            principalName,
            new ChatErrorResponse(
                exception.code(),
                resolveLlmMessage(exception.code()),
                normalizedClientMessageId,
                Instant.now()
            ),
            sessionId
        );
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

    private String resolveLlmMessage(String code) {
        return switch (code) {
            case "CHAT_LLM_AUTH" -> "Chat service authentication failed.";
            case "CHAT_LLM_RATE_LIMIT" -> "Chat service is busy. Please try again shortly.";
            case "CHAT_LLM_TIMEOUT" -> "Chat service timed out. Please retry.";
            case "CHAT_LLM_EMPTY" -> "Chat service returned an empty response.";
            case "CHAT_LLM_PARSE_ERROR" -> "Chat service returned an invalid response.";
            case "CHAT_LLM_UPSTREAM" -> "Chat service is temporarily unavailable.";
            default -> "Unable to process chat request.";
        };
    }
}
