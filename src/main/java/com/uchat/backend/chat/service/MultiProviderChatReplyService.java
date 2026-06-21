package com.uchat.backend.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchat.backend.config.UChatProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "uchat.llm", name = "enabled", havingValue = "true")
public class MultiProviderChatReplyService implements ChatReplyService {

    private static final Logger logger = LoggerFactory.getLogger(MultiProviderChatReplyService.class);

    private final UChatProperties.LlmProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MultiProviderChatReplyService(UChatProperties uChatProperties) {
        this.properties = uChatProperties.llm();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.timeoutMs()))
                .build();
    }

    @Override
    public String generateReply(ChatRequestContext requestContext) {
        validateConfig();

        int attempts = Math.max(1, properties.maxRetries() + 1);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return doGenerateReply(requestContext);
            } catch (LlmServiceException ex) {
                if (!isRetryable(ex) || attempt == attempts) {
                    throw ex;
                }
                logger.warn("LLM request failed with code {} (attempt {}/{}), retrying", ex.code(), attempt, attempts);
                sleepBackoff();
            }
        }
        throw new LlmServiceException("CHAT_LLM_UPSTREAM", "LLM request failed after retries.");
    }

    private String doGenerateReply(ChatRequestContext requestContext) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(buildChatUri())
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .headers(buildHeaders())
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(requestContext)))
                    .build();
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_PARSE_ERROR", "Failed to serialize LLM request.", ex);
        }

        HttpResponse<java.util.stream.Stream<String>> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_UPSTREAM", "Unable to reach LLM service.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new LlmServiceException("CHAT_LLM_TIMEOUT", "LLM request was interrupted.", ex);
        }

        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new LlmServiceException("CHAT_LLM_AUTH", "LLM authentication failed.");
        }
        if (response.statusCode() == 429) {
            throw new LlmServiceException("CHAT_LLM_RATE_LIMIT", "LLM rate limit reached.");
        }
        if (response.statusCode() >= 500) {
            throw new LlmServiceException("CHAT_LLM_UPSTREAM", "LLM service is temporarily unavailable.");
        }
        if (response.statusCode() >= 400) {
            throw new LlmServiceException("CHAT_BAD_REQUEST", "Invalid request to LLM service.");
        }

        StringBuilder aggregated = new StringBuilder();
        try (java.util.stream.Stream<String> stream = response.body()) {
            stream.forEach(line -> processStreamLine(line, aggregated));
        }

        String result = aggregated.toString().trim();
        if (result.isEmpty()) {
            result = doGenerateReplyNonStreaming(requestContext).trim();
        }
        if (result.isEmpty()) {
            throw new LlmServiceException("CHAT_LLM_EMPTY", "LLM returned an empty response.");
        }
        return result;
    }

    private String doGenerateReplyNonStreaming(ChatRequestContext requestContext) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(buildChatUri())
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .headers(buildHeaders())
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(requestContext, false)))
                    .build();
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_PARSE_ERROR", "Failed to serialize fallback request.", ex);
        }

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_UPSTREAM", "Unable to reach LLM service.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new LlmServiceException("CHAT_LLM_TIMEOUT", "LLM fallback request was interrupted.", ex);
        }

        if (response.statusCode() >= 400) {
            return "";
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            return extractNonStreamContent(provider(), root);
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_PARSE_ERROR", "Failed to parse fallback response.", ex);
        }
    }

    private void processStreamLine(String rawLine, StringBuilder aggregated) {
        if (rawLine == null || rawLine.isBlank()) {
            return;
        }
        String line = rawLine.trim();
        if (!line.startsWith("data:")) {
            return;
        }
        String data = line.substring("data:".length()).trim();
        if ("[DONE]".equals(data)) {
            return;
        }

        String provider = provider();
        try {
            JsonNode root = objectMapper.readTree(data);
            String content = extractContent(provider, root);
            if (!content.isEmpty()) {
                appendStreamContent(provider, aggregated, content);
            } else if ("minimax".equals(provider)) {
                logger.debug("MiniMax stream chunk without content: {}", data);
            }
        } catch (IOException ex) {
            throw new LlmServiceException("CHAT_LLM_PARSE_ERROR", "Failed to parse streaming response from LLM.", ex);
        }
    }

    private String extractContent(String provider, JsonNode root) {
        return switch (provider) {
            case "anthropic", "claude" -> root.path("delta").path("text").asText("");
            case "gemini" -> {
                String chunk = extractOpenAiStreamContent(root);
                yield chunk.isEmpty()
                        ? root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("")
                        : chunk;
            }
            case "minimax" -> {
                String chunk = extractOpenAiStreamContent(root);
                if (!chunk.isEmpty()) {
                    yield chunk;
                }
                chunk = root.path("reply").asText("");
                if (!chunk.isEmpty()) {
                    yield chunk;
                }
                yield root.path("text").asText("");
            }
            case "qwen" -> {
                String chunk = extractOpenAiStreamContent(root);
                yield chunk.isEmpty() ? root.path("output").path("text").asText("") : chunk;
            }
            default -> {
                String chunk = extractOpenAiStreamContent(root);
                yield chunk.isEmpty() && "gemini".equals(provider)
                        ? root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("")
                        : chunk;
            }
        };
    }

    private void appendStreamContent(String provider, StringBuilder aggregated, String content) {
        if ("minimax".equals(provider)) {
            String current = aggregated.toString();
            if (content.equals(current) || current.endsWith(content)) {
                return;
            }
            if (content.startsWith(current)) {
                aggregated.setLength(0);
                aggregated.append(content);
                return;
            }
        }
        aggregated.append(content);
    }

    private String extractNonStreamContent(String provider, JsonNode root) {
        return switch (provider) {
            case "anthropic", "claude" -> root.path("content").path(0).path("text").asText("");
            case "gemini" -> {
                String text = extractOpenAiMessageContent(root);
                yield text.isEmpty()
                        ? root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("")
                        : text;
            }
            case "minimax" -> {
                String text = extractOpenAiMessageContent(root);
                if (!text.isEmpty()) {
                    yield text;
                }
                text = root.path("reply").asText("");
                if (!text.isEmpty()) {
                    yield text;
                }
                yield root.path("text").asText("");
            }
            case "qwen" -> {
                String text = extractOpenAiMessageContent(root);
                yield text.isEmpty() ? root.path("output").path("text").asText("") : text;
            }
            default -> {
                String text = extractOpenAiMessageContent(root);
                yield text.isEmpty() && "gemini".equals(provider)
                        ? root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("")
                        : text;
            }
        };
    }

    private String extractOpenAiStreamContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            String content = choices.path(0).path("delta").path("content").asText("");
            if (!content.isEmpty()) {
                return content;
            }
            return choices.path(0).path("message").path("content").asText("");
        }
        return "";
    }

    private String extractOpenAiMessageContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            return choices.path(0).path("message").path("content").asText("");
        }
        return "";
    }

    private URI buildChatUri() {
        String provider = provider();
        String base = Objects.requireNonNullElse(properties.baseUrl(), "").trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        return switch (provider) {
            case "gemini" -> URI.create(base + "/v1beta/openai/chat/completions");
            case "minimax" -> URI.create(base + (base.endsWith("/v1") ? "/text/chatcompletion_v2" : "/v1/text/chatcompletion_v2"));
            case "qwen" -> URI.create(base + "/compatible-mode/v1/chat/completions");
            case "anthropic", "claude" -> URI.create(base + "/v1/messages");
            default -> URI.create(base + "/v1/chat/completions");
        };
    }

    private String[] buildHeaders() {
        if ("anthropic".equals(provider()) || "claude".equals(provider())) {
            return new String[] {
                    "Content-Type", "application/json",
                    "x-api-key", properties.apiKey(),
                    "anthropic-version", "2023-06-01"
            };
        }
        return new String[] {
                "Content-Type", "application/json",
                "Authorization", "Bearer " + properties.apiKey()
        };
    }

    private String buildRequestBody(ChatRequestContext requestContext) throws IOException {
        return buildRequestBody(requestContext, true);
    }

    private String buildRequestBody(ChatRequestContext requestContext, boolean stream) throws IOException {
        String provider = provider();
        return switch (provider) {
            case "anthropic", "claude" -> objectMapper.writeValueAsString(buildAnthropicBody(requestContext, stream));
            case "gemini", "minimax", "qwen", "openai" -> objectMapper.writeValueAsString(buildOpenAiCompatibleBody(requestContext, stream));
            default -> throw new LlmServiceException("CHAT_INTERNAL_ERROR", "Unsupported LLM provider: " + properties.provider());
        };
    }

    private Map<String, Object> buildOpenAiCompatibleBody(ChatRequestContext requestContext, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("temperature", properties.temperature());
        body.put("max_tokens", properties.maxTokens());
        body.put("stream", stream);
        body.put("messages", buildMessages(requestContext));
        return body;
    }

    private Map<String, Object> buildAnthropicBody(ChatRequestContext requestContext, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("temperature", properties.temperature());
        body.put("max_tokens", properties.maxTokens());
        body.put("stream", stream);
        body.put("system", systemPromptForLocale(requestContext.locale()));

        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatRequestContext.ConversationTurn turn : requestContext.history()) {
            String role = normalizeRole(turn.role());
            if (!"system".equals(role) && turn.content() != null && !turn.content().isBlank()) {
                messages.add(message(role, turn.content()));
            }
        }
        messages.add(message("user", requestContext.content()));
        body.put("messages", messages);
        return body;
    }

    private List<Map<String, String>> buildMessages(ChatRequestContext requestContext) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPromptForLocale(requestContext.locale())));

        for (ChatRequestContext.ConversationTurn turn : requestContext.history()) {
            String role = normalizeRole(turn.role());
            if (turn.content() != null && !turn.content().isBlank()) {
                messages.add(message(role, turn.content()));
            }
        }
        messages.add(message("user", requestContext.content()));
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "user";
        }
        String normalized = role.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "assistant", "system", "user" -> normalized;
            default -> "user";
        };
    }

    private String systemPromptForLocale(String locale) {
        String basePrompt = properties.systemPrompt();
        if (basePrompt == null || basePrompt.isBlank()) {
            basePrompt = "You are uChat's enterprise service assistant. Reply clearly and concisely in the user's language.";
        }
        if ("zh".equalsIgnoreCase(locale)) {
            return basePrompt + " Use Simplified Chinese for responses.";
        }
        return basePrompt + " Use English for responses.";
    }

    private void validateConfig() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new LlmServiceException("CHAT_LLM_AUTH", provider().toUpperCase(Locale.ROOT) + " API key is missing.");
        }
        if (properties.baseUrl() == null || properties.baseUrl().isBlank()) {
            throw new LlmServiceException("CHAT_INTERNAL_ERROR", "LLM base URL is not configured.");
        }
        if (properties.model() == null || properties.model().isBlank()) {
            throw new LlmServiceException("CHAT_INTERNAL_ERROR", "LLM model is not configured.");
        }
        if (!List.of("openai", "gemini", "anthropic", "qwen", "minimax", "claude").contains(provider())) {
            throw new LlmServiceException("CHAT_INTERNAL_ERROR", "Unsupported LLM provider: " + properties.provider());
        }
    }

    private String provider() {
        return Objects.requireNonNullElse(properties.provider(), "openai").trim().toLowerCase(Locale.ROOT);
    }

    private boolean isRetryable(LlmServiceException exception) {
        return "CHAT_LLM_RATE_LIMIT".equals(exception.code())
                || "CHAT_LLM_UPSTREAM".equals(exception.code())
                || "CHAT_LLM_TIMEOUT".equals(exception.code());
    }

    private void sleepBackoff() {
        try {
            Thread.sleep(Math.max(0, properties.retryBackoffMs()));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
