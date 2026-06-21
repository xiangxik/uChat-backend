package com.uchat.backend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchat.backend.chat.dto.ChatErrorResponse;
import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.config.UChatProperties;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatUserMessagingGatewayTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UChatProperties properties;

    private ChatUserMessagingGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new ChatUserMessagingGateway(messagingTemplate, properties);
        when(properties.userDestinationPrefix()).thenReturn("/user");
    }

    @Test
    void sendsChatMessageToStrippedUserDestinationWithoutSessionHeaders() {
        ChatMessageResponse response = new ChatMessageResponse(
                "bot-1",
                "client-1",
                "conv-1",
                "bot",
                "ok",
                Instant.now()
        );
        when(properties.chatMessageSubscription()).thenReturn("/user/queue/chat.messages");

        gateway.sendChatMessage("user-123", response, null);

        verify(messagingTemplate).convertAndSendToUser(
                eq("user-123"),
                eq("/queue/chat.messages"),
                eq(response)
        );
    }

    @Test
    void sendsChatErrorWithSessionHeaderWhenSessionIsPresent() {
        ChatErrorResponse error = new ChatErrorResponse("CHAT_BAD_REQUEST", "bad", "client-1", Instant.now());
        when(properties.chatErrorSubscription()).thenReturn("/user/queue/chat.errors");

        gateway.sendChatError("user-123", error, "session-1");

        ArgumentCaptor<MessageHeaders> headerCaptor = ArgumentCaptor.forClass(MessageHeaders.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("user-123"),
                eq("/queue/chat.errors"),
                eq(error),
                headerCaptor.capture()
        );

        MessageHeaders headers = headerCaptor.getValue();
        assertThat(headers.get(SimpMessageHeaderAccessor.SESSION_ID_HEADER)).isEqualTo("session-1");
    }
}
