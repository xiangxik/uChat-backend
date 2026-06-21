package com.uchat.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.uchat.backend.config.UChatProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MultiProviderChatReplyServiceTest {

        @Test
        void minimaxUsesConfiguredEndpointAndBearerAuth() throws IOException {
                AtomicReference<String> path = new AtomicReference<>();
                AtomicReference<String> authorization = new AtomicReference<>();
                AtomicReference<String> body = new AtomicReference<>();
                HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/v1/text/chatcompletion_v2", exchange -> {
                        path.set(exchange.getRequestURI().getPath());
                        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                        body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                        sendResponse(exchange, "data: {\"choices\":[{\"delta\":{\"content\":\"hello from minimax\"}}]}\n\ndata: [DONE]\n");
                });
                server.start();
                try {
                        MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties(
                                        "minimax",
                                        "http://127.0.0.1:" + server.getAddress().getPort(),
                                        "test-minimax-key",
                                        "MiniMax-Text-01"
                        ));

                        String reply = service.generateReply(requestContext());

                        assertThat(reply).isEqualTo("hello from minimax");
                        assertThat(path).hasValue("/v1/text/chatcompletion_v2");
                        assertThat(authorization).hasValue("Bearer test-minimax-key");
                        assertThat(body.get()).contains("\"model\":\"MiniMax-Text-01\"");
                } finally {
                        server.stop(0);
                }
        }

                @Test
                void minimaxDoesNotDuplicateRepeatedStreamChunks() throws IOException {
                        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                        server.createContext("/v1/text/chatcompletion_v2", exchange -> sendResponse(exchange,
                                        "data: {\"choices\":[{\"delta\":{\"content\":\"你好！请问有什么我可以帮助你的吗？\"}}]}\n\n"
                                                        + "data: {\"choices\":[{\"delta\":{\"content\":\"你好！请问有什么我可以帮助你的吗？\"}}]}\n\n"
                                                        + "data: [DONE]\n"));
                        server.start();
                        try {
                                MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties(
                                                "minimax",
                                                "http://127.0.0.1:" + server.getAddress().getPort(),
                                                "test-minimax-key",
                                                "MiniMax-M3"
                                ));

                                String reply = service.generateReply(requestContext());

                                assertThat(reply).isEqualTo("你好！请问有什么我可以帮助你的吗？");
                        } finally {
                                server.stop(0);
                        }
                }

                @Test
                void minimaxUsesLatestCumulativeStreamChunk() throws IOException {
                        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                        server.createContext("/v1/text/chatcompletion_v2", exchange -> sendResponse(exchange,
                                        "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}\n\n"
                                                        + "data: {\"choices\":[{\"delta\":{\"content\":\"你好世界\"}}]}\n\n"
                                                        + "data: [DONE]\n"));
                        server.start();
                        try {
                                MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties(
                                                "minimax",
                                                "http://127.0.0.1:" + server.getAddress().getPort(),
                                                "test-minimax-key",
                                                "MiniMax-M3"
                                ));

                                String reply = service.generateReply(requestContext());

                                assertThat(reply).isEqualTo("你好世界");
                        } finally {
                                server.stop(0);
                        }
                }

        @Test
        void anthropicUsesApiKeyHeaderAndMessagesEndpoint() throws IOException {
                AtomicReference<String> path = new AtomicReference<>();
                AtomicReference<String> apiKey = new AtomicReference<>();
                AtomicReference<String> anthropicVersion = new AtomicReference<>();
                HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/v1/messages", exchange -> {
                        path.set(exchange.getRequestURI().getPath());
                        apiKey.set(exchange.getRequestHeaders().getFirst("x-api-key"));
                        anthropicVersion.set(exchange.getRequestHeaders().getFirst("anthropic-version"));
                        sendResponse(exchange, "data: {\"delta\":{\"text\":\"hello from anthropic\"}}\n\ndata: [DONE]\n");
                });
                server.start();
                try {
                        MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties(
                                        "anthropic",
                                        "http://127.0.0.1:" + server.getAddress().getPort(),
                                        "test-anthropic-key",
                                        "claude-3-5-haiku-latest"
                        ));

                        String reply = service.generateReply(requestContext());

                        assertThat(reply).isEqualTo("hello from anthropic");
                        assertThat(path).hasValue("/v1/messages");
                        assertThat(apiKey).hasValue("test-anthropic-key");
                        assertThat(anthropicVersion).hasValue("2023-06-01");
                } finally {
                        server.stop(0);
                }
        }

        @Test
        void geminiParsesOpenAiCompatibleStreamResponse() throws IOException {
                HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/v1beta/openai/chat/completions", exchange ->
                                sendResponse(exchange, "data: {\"choices\":[{\"delta\":{\"content\":\"hello from gemini\"}}]}\n\ndata: [DONE]\n"));
                server.start();
                try {
                        MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties(
                                        "gemini",
                                        "http://127.0.0.1:" + server.getAddress().getPort(),
                                        "test-gemini-key",
                                        "gemini-2.5-flash"
                        ));

                        String reply = service.generateReply(requestContext());

                        assertThat(reply).isEqualTo("hello from gemini");
                } finally {
                        server.stop(0);
                }
        }

    @Test
    void failsFastWhenApiKeyMissing() {
        UChatProperties properties = properties("openai", "https://api.openai.com", "", "gpt-4.1-mini");

        MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties);

        assertThatThrownBy(() -> service.generateReply(requestContext()))
                .isInstanceOf(LlmServiceException.class)
                .hasMessageContaining("OPENAI API key is missing");
    }

    @Test
    void failsFastWhenProviderUnsupported() {
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
                        "unknown-provider",
                        "https://api.openai.com",
                        "test-key",
                        "gpt-4.1-mini",
                        0.4,
                        1000,
                        20000,
                        1,
                        300,
                        10,
                        "system"
                )
        );

        MultiProviderChatReplyService service = new MultiProviderChatReplyService(properties);

        assertThatThrownBy(() -> service.generateReply(requestContext()))
                .isInstanceOf(LlmServiceException.class)
                .hasMessageContaining("Unsupported LLM provider");
    }

    private static UChatProperties properties(String provider, String baseUrl, String apiKey, String model) {
        return new UChatProperties(
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
                        provider,
                        baseUrl,
                        apiKey,
                        model,
                        0.4,
                        1000,
                        20000,
                        1,
                        300,
                        10,
                        "system"
                )
        );
    }

    private static ChatRequestContext requestContext() {
        return new ChatRequestContext(
                "conv-1",
                "client-1",
                "user-1",
                "hello",
                "en",
                List.of()
        );
    }

    private static void sendResponse(HttpExchange exchange, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
