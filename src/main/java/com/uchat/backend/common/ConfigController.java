package com.uchat.backend.common;

import com.uchat.backend.config.UChatProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final UChatProperties properties;

    public ConfigController(UChatProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public AppConfigResponse getConfig() {
        return new AppConfigResponse(
                properties.appName(),
                properties.defaultLocale(),
                properties.websocketEndpoint(),
                properties.chatSendDestination(),
                properties.chatMessageSubscription(),
                properties.chatErrorSubscription()
        );
    }
}
