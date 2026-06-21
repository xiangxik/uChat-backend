package com.uchat.backend.common;

public record AppConfigResponse(
        String appName,
        String defaultLocale,
        String webSocketEndpoint,
        String chatSendDestination,
        String chatMessageSubscription,
        String chatErrorSubscription
) {
}
