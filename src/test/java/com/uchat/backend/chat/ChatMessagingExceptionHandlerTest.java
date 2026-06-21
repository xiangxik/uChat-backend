package com.uchat.backend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.uchat.backend.chat.dto.ChatErrorResponse;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessagingExceptionHandlerTest {

    @Mock
    private ChatUserMessagingGateway chatUserMessagingGateway;

    private ChatMessagingExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatMessagingExceptionHandler(chatUserMessagingGateway);
    }

    @Test
    void propagatesClientMessageIdForBadRequest() {
        Principal principal = () -> "user-1";

        handler.handleBadRequest(
                new IllegalArgumentException("locale must be zh or en"),
                principal,
                "client-1",
                "session-1"
        );

        ArgumentCaptor<ChatErrorResponse> payloadCaptor = ArgumentCaptor.forClass(ChatErrorResponse.class);
        verify(chatUserMessagingGateway).sendChatError(
                eq("user-1"),
                payloadCaptor.capture(),
                eq("session-1")
        );

        ChatErrorResponse payload = payloadCaptor.getValue();
        assertThat(payload.code()).isEqualTo("CHAT_BAD_REQUEST");
        assertThat(payload.message()).isEqualTo("locale must be zh or en");
        assertThat(payload.clientMessageId()).isEqualTo("client-1");
    }

    @Test
    void propagatesClientMessageIdForUnexpectedException() {
        Principal principal = () -> "user-2";

        handler.handleUnexpected(
                new RuntimeException("boom"),
                principal,
                "client-2",
                "session-2"
        );

        ArgumentCaptor<ChatErrorResponse> payloadCaptor = ArgumentCaptor.forClass(ChatErrorResponse.class);
        verify(chatUserMessagingGateway).sendChatError(
                eq("user-2"),
                payloadCaptor.capture(),
                eq("session-2")
        );

        ChatErrorResponse payload = payloadCaptor.getValue();
        assertThat(payload.code()).isEqualTo("CHAT_INTERNAL_ERROR");
        assertThat(payload.message()).isEqualTo("Unable to process chat request.");
        assertThat(payload.clientMessageId()).isEqualTo("client-2");
    }
}
