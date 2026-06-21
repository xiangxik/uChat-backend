package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import com.uchat.backend.config.UChatProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMessageApplicationServiceTest {

    @Mock
    private ChatReplyService chatReplyService;

    @Mock
    private InMemoryConversationHistoryStore historyStore;

    private ChatMessageApplicationService service;

    @BeforeEach
    void setUp() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors",
                new UChatProperties.LlmProperties(
                        true,
                        "openai",
                        "https://api.openai.com",
                        "test-key",
                        "gpt-4.1-mini",
                        0.4,
                        1000,
                        20000,
                        1,
                        300,
                        3,
                        "system"
                )
        );
        service = new ChatMessageApplicationService(chatReplyService, historyStore, properties);
    }

    @Test
    void createsReplyUsingRecentConversationHistory() {
        ChatSendRequest request = new ChatSendRequest("conv-1", "client-1", "Need a loan", "en");
        List<ChatRequestContext.ConversationTurn> history = List.of(
                new ChatRequestContext.ConversationTurn("user", "hello"),
                new ChatRequestContext.ConversationTurn("assistant", "hi")
        );
        when(historyStore.recentTurns("conv-1", 3)).thenReturn(history);
        when(chatReplyService.generateReply(org.mockito.ArgumentMatchers.any(ChatRequestContext.class)))
                .thenReturn("reply text");

        ChatMessageResponse response = service.createBotMessage(request, "user-1");

        ArgumentCaptor<ChatRequestContext> contextCaptor = forClass(ChatRequestContext.class);
        verify(chatReplyService).generateReply(contextCaptor.capture());
        ChatRequestContext context = contextCaptor.getValue();
        assertThat(context.conversationId()).isEqualTo("conv-1");
        assertThat(context.clientMessageId()).isEqualTo("client-1");
        assertThat(context.principalName()).isEqualTo("user-1");
        assertThat(context.content()).isEqualTo("Need a loan");
        assertThat(context.locale()).isEqualTo("en");
        assertThat(context.history()).containsExactlyElementsOf(history);

        verify(historyStore).appendUserTurn("conv-1", "Need a loan");
        verify(historyStore).appendBotTurn("conv-1", "reply text");

        assertThat(response.clientMessageId()).isEqualTo("client-1");
        assertThat(response.conversationId()).isEqualTo("conv-1");
        assertThat(response.sender()).isEqualTo("bot");
        assertThat(response.content()).isEqualTo("reply text");
    }
}
