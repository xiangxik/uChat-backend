package com.uchat.backend.chat;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import com.uchat.backend.chat.service.ChatMessageApplicationService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatMessageApplicationService chatMessageApplicationService;
    private final ChatUserMessagingGateway chatUserMessagingGateway;

    public ChatController(
            ChatMessageApplicationService chatMessageApplicationService,
            ChatUserMessagingGateway chatUserMessagingGateway
    ) {
        this.chatMessageApplicationService = chatMessageApplicationService;
        this.chatUserMessagingGateway = chatUserMessagingGateway;
    }

    @MessageMapping("${uchat.chat-send-mapping}")
    public void send(
            @Valid ChatSendRequest request,
            Principal principal,
            @Header(name = "simpSessionId", required = false) String sessionId
    ) {
        logger.info("Chat request received for conversation {} from principal {}",
                request.conversationId(), principal != null ? principal.getName() : "anonymous");
        ChatMessageResponse response = chatMessageApplicationService.createBotMessage(request);
        String principalName = principal != null ? principal.getName() : "anonymous";
        chatUserMessagingGateway.sendChatMessage(principalName, response, sessionId);
        logger.info("Chat response generated for conversation {} with message {}",
                request.conversationId(), response.id());
    }
}
