package com.uchat.backend.config;

import java.security.Principal;
import java.util.UUID;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UChatProperties properties;

    public WebSocketConfig(UChatProperties properties) {
        this.properties = properties;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(properties.websocketEndpoint())
                .setAllowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      java.util.Map<String, Object> attributes) {
                        return new WebSocketPrincipal("user-" + UUID.randomUUID());
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(properties.applicationDestinationPrefix());
        registry.enableSimpleBroker(properties.brokerDestinations().toArray(String[]::new));
        registry.setUserDestinationPrefix(properties.userDestinationPrefix());
    }
}
