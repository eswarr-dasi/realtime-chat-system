package com.eswarr.chat.config;

import com.eswarr.chat.security.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket + STOMP configuration.
 *
 * - /ws is the WebSocket handshake endpoint (with SockJS fallback)
 * - /topic/** is used for broadcasting to subscribers (pub-sub)
 * - /app/** is used for messages sent FROM client TO server
 * - JwtHandshakeInterceptor validates the JWT token during handshake
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(jwtHandshakeInterceptor)
            .withSockJS();

        // Also expose a raw WebSocket endpoint (no SockJS) for native clients
        registry.addEndpoint("/ws-raw")
            .setAllowedOriginPatterns("*")
            .addInterceptors(jwtHandshakeInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker for /topic and /queue destinations.
        // Redis pub/sub (RedisMessageRelay) bridges messages ACROSS instances,
        // while this in-memory broker delivers to LOCAL subscribers.
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
