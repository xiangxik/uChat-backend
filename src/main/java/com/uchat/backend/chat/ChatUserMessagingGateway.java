package com.uchat.backend.chat;

import com.uchat.backend.chat.dto.ChatErrorResponse;
import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.config.UChatProperties;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatUserMessagingGateway {

    private final SimpMessagingTemplate messagingTemplate;
    private final UChatProperties properties;

    public ChatUserMessagingGateway(SimpMessagingTemplate messagingTemplate, UChatProperties properties) {
        this.messagingTemplate = messagingTemplate;
        this.properties = properties;
    }

    public void sendChatMessage(String principalName, ChatMessageResponse payload, String sessionId) {
        sendToUser(principalName, properties.chatMessageSubscription(), payload, sessionId);
    }

    public void sendChatError(String principalName, ChatErrorResponse payload, String sessionId) {
        sendToUser(principalName, properties.chatErrorSubscription(), payload, sessionId);
    }

    private void sendToUser(String principalName, String configuredDestination, Object payload, String sessionId) {
        String destination = resolveUserQueueDestination(configuredDestination);
        if (sessionId == null) {
            messagingTemplate.convertAndSendToUser(principalName, destination, payload);
            return;
        }
        messagingTemplate.convertAndSendToUser(principalName, destination, payload, createHeaders(sessionId));
    }

    private String resolveUserQueueDestination(String configuredDestination) {
        String userPrefix = properties.userDestinationPrefix();
        if (configuredDestination.startsWith(userPrefix)) {
            return configuredDestination.substring(userPrefix.length());
        }
        return configuredDestination;
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor.getMessageHeaders();
    }
}
