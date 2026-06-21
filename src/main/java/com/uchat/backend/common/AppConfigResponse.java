package com.uchat.backend.common;

import java.util.List;

public record AppConfigResponse(
        String appName,
        String defaultLocale,
        String locale,
        String initialBotMessage,
        String messagePlaceholder,
        String sendLabel,
        String tipText,
        String thankYouText,
        String thinkingText,
        String serviceCenterText,
        String onlineText,
        List<String> ratingLabels,
        String webSocketEndpoint,
        String chatSendDestination,
        String chatMessageSubscription,
        String chatErrorSubscription
) {
}
