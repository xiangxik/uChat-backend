package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchat.backend.chat.dto.ChatMessageResponse;
import com.uchat.backend.chat.dto.ChatSendRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "uchat.llm.enabled=true",
        "uchat.llm.provider=minimax"
})
class MinimaxLiveCallTest {

    @Autowired
    private ChatMessageApplicationService chatMessageApplicationService;

    @Test
    void canCallMinimaxAndGetNonEmptyReply() {
        ChatSendRequest request = new ChatSendRequest(
                "conv-live-1",
                "client-live-1",
                "请用一句话介绍你自己",
                "zh"
        );

        ChatMessageResponse response = chatMessageApplicationService.createBotMessage(request, "live-user");

        assertThat(response.clientMessageId()).isEqualTo("client-live-1");
        assertThat(response.conversationId()).isEqualTo("conv-live-1");
        assertThat(response.sender()).isEqualTo("bot");
        assertThat(response.content()).isNotBlank();
    }
}
