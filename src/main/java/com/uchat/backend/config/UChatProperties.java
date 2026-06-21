package com.uchat.backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "uchat")
public record UChatProperties(
        String appName,
        String defaultLocale,
        List<String> allowedOrigins,
        String applicationDestinationPrefix,
        String userDestinationPrefix,
        List<String> brokerDestinations,
        String chatSendMapping,
        String websocketEndpoint,
        String chatSendDestination,
        String chatMessageSubscription,
        String chatErrorSubscription,
        LlmProperties llm
) {

    public record LlmProperties(
            boolean enabled,
            String provider,
            String baseUrl,
            String apiKey,
            String model,
            double temperature,
            int maxTokens,
            int timeoutMs,
            int maxRetries,
            int retryBackoffMs,
            int contextWindow,
            String systemPrompt
    ) {
    }
}
