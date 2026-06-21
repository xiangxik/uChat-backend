package com.uchat.backend.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchat.backend.chat.dto.ChatErrorResponse;
import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.config.WebSocketPrincipal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

@SpringBootTest(properties = "uchat.llm.enabled=false")
class ChatWebSocketIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${uchat.chat-send-destination}")
    private String chatSendDestination;

    @Value("${uchat.chat-message-subscription}")
    private String chatMessageSubscription;

    @Value("${uchat.chat-error-subscription}")
    private String chatErrorSubscription;

    @Autowired
    @Qualifier("clientInboundChannel")
    private MessageChannel clientInboundChannel;

    @Autowired
    @Qualifier("brokerChannel")
    private SubscribableChannel brokerChannel;

    private final BlockingQueue<Message<?>> brokerMessages = new LinkedBlockingQueue<>();
    private MessageHandler brokerHandler;

    @BeforeEach
    void subscribeBrokerChannel() {
        brokerHandler = brokerMessages::offer;
        brokerChannel.subscribe(brokerHandler);
    }

    @AfterEach
    void unsubscribeBrokerChannel() {
        if (brokerHandler != null) {
            brokerChannel.unsubscribe(brokerHandler);
        }
        brokerMessages.clear();
    }

    @Test
    void returnsBotMessageForValidRequest() throws Exception {
        String sessionId = "session-valid";
        String principalName = "user-valid";

        sendChatMessage(sessionId, principalName, "conv-it-1", "client-it-1", "loan", "en");

        Message<?> brokerMessage = pollMessage(message -> hasSessionId(message, sessionId));
        assertThat(brokerMessage).isNotNull();
        assertThat(destinationOf(brokerMessage)).contains("/queue/chat.messages");
        assertThat(destinationOf(brokerMessage)).contains(principalName);

        ChatMessageResponse response = readPayload(brokerMessage, ChatMessageResponse.class);
        assertThat(response.clientMessageId()).isEqualTo("client-it-1");
        assertThat(response.conversationId()).isEqualTo("conv-it-1");
        assertThat(response.sender()).isEqualTo("bot");
        assertThat(response.content()).isNotBlank();
    }

    @Test
    void returnsStructuredErrorForInvalidLocale() throws Exception {
        String sessionId = "session-error";
        String principalName = "user-error";

        sendChatMessage(sessionId, principalName, "conv-it-2", "client-it-2", "loan", "fr");

        Message<?> brokerMessage = pollMessage(message -> hasSessionId(message, sessionId));
        assertThat(brokerMessage).isNotNull();
        assertThat(destinationOf(brokerMessage)).contains("/queue/chat.errors");
        assertThat(destinationOf(brokerMessage)).contains(principalName);

        ChatErrorResponse error = readPayload(brokerMessage, ChatErrorResponse.class);
        assertThat(error.code()).isEqualTo("CHAT_BAD_REQUEST");
        assertThat(error.message()).contains("locale must be zh or en");
    }

    @Test
    void correlatesConcurrentRequestsByClientMessageId() throws Exception {
        String sessionId = "session-concurrent";
        String principalName = "user-concurrent";

        sendChatMessage(sessionId, principalName, "conv-it-3", "client-it-3a", "loan", "en");
        sendChatMessage(sessionId, principalName, "conv-it-3", "client-it-3b", "account", "en");

        Message<?> first = pollMessage(message -> hasSessionId(message, sessionId));
        Message<?> second = pollMessage(message -> hasSessionId(message, sessionId));

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();

        ChatMessageResponse response1 = readPayload(first, ChatMessageResponse.class);
        ChatMessageResponse response2 = readPayload(second, ChatMessageResponse.class);

        assertThat(java.util.Set.of(response1.clientMessageId(), response2.clientMessageId()))
                .containsExactlyInAnyOrder("client-it-3a", "client-it-3b");
    }

    private void sendChatMessage(
            String sessionId,
            String principalName,
            String conversationId,
            String clientMessageId,
            String content,
            String locale
    ) throws Exception {
        SimpMessageHeaderAccessor accessor = createAccessor(SimpMessageType.MESSAGE, sessionId, principalName);
        accessor.setDestination(chatSendDestination);
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);

        byte[] payload = objectMapper.writeValueAsBytes(new ChatRequestPayload(
                conversationId,
                clientMessageId,
                content,
                locale
        ));

        clientInboundChannel.send(MessageBuilder.createMessage(payload, accessor.getMessageHeaders()));
    }

    private SimpMessageHeaderAccessor createAccessor(SimpMessageType messageType, String sessionId, String principalName) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(messageType);
        accessor.setSessionId(sessionId);
        accessor.setSessionAttributes(newSessionAttributes());
        accessor.setUser(new WebSocketPrincipal(principalName));
        accessor.setLeaveMutable(true);
        return accessor;
    }

    private Map<String, Object> newSessionAttributes() {
        return new HashMap<>();
    }

    private Message<?> pollMessage(Predicate<Message<?>> matcher) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Message<?> message = brokerMessages.poll(200, TimeUnit.MILLISECONDS);
            if (message != null && matcher.test(message)) {
                return message;
            }
        }
        return null;
    }

    private boolean hasSessionId(Message<?> message, String sessionId) {
        Object actual = message.getHeaders().get(SimpMessageHeaderAccessor.SESSION_ID_HEADER);
        return sessionId.equals(actual);
    }

    private String destinationOf(Message<?> message) {
        Object destination = message.getHeaders().get(SimpMessageHeaderAccessor.DESTINATION_HEADER);
        return destination == null ? "" : destination.toString();
    }

    private <T> T readPayload(Message<?> message, Class<T> payloadType) throws Exception {
        Object payload = message.getPayload();
        if (payloadType.isInstance(payload)) {
            return payloadType.cast(payload);
        }
        if (payload instanceof byte[] bytes) {
            return objectMapper.readValue(bytes, payloadType);
        }
        if (payload instanceof String text) {
            return objectMapper.readValue(text, payloadType);
        }
        return objectMapper.readValue(payload.toString().getBytes(StandardCharsets.UTF_8), payloadType);
    }

    private record ChatRequestPayload(
            String conversationId,
            String clientMessageId,
            String content,
            String locale
    ) {
    }
}
