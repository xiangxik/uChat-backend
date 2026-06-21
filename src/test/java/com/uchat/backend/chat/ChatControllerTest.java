package com.uchat.backend.chat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import com.uchat.backend.chat.service.ChatMessageApplicationService;
import java.time.Instant;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatMessageApplicationService chatMessageApplicationService;

    @Mock
    private ChatUserMessagingGateway chatUserMessagingGateway;

    @InjectMocks
    private ChatController controller;

    private Principal principal;

    @BeforeEach
    void setUp() {
        principal = () -> "user-123";
    }

    @Test
    void sendsBotReplyToCurrentUserQueue() {
        ChatSendRequest request = new ChatSendRequest("conv-1", "client-1", "   ", "en");
        ChatMessageResponse generatedResponse = new ChatMessageResponse(
                "bot-1",
                "client-1",
                "conv-1",
                "bot",
                "Please type your question first.",
                Instant.now()
        );
        when(chatMessageApplicationService.createBotMessage(request)).thenReturn(generatedResponse);

        controller.send(request, principal, null);

        verify(chatUserMessagingGateway).sendChatMessage(
                eq("user-123"),
            eq(generatedResponse),
            eq(null)
        );
    }

}
