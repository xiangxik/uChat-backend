package com.uchat.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "uchat.storage")
public record StorageProperties(
        String provider
) {
}
