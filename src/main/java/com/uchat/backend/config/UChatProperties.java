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
        String chatErrorSubscription
) {
}
