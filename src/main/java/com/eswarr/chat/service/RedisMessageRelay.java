package com.eswarr.chat.service;

import com.eswarr.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Bridges chat messages between server instances using Redis Pub/Sub.
 *
 * THE KEY TO HORIZONTAL SCALING:
 *   - When a message arrives on Server A (from a WebSocket client), it is
 *     PUBLISHED to a Redis channel instead of only being broadcast locally.
 *   - Every server instance (A, B, C...) SUBSCRIBES to that Redis channel.
 *   - When Redis delivers the message to each instance, each instance uses
 *     its local SimpMessagingTemplate to push it to its own locally-connected
 *     WebSocket clients.
 *
 * Result: a client connected to Server B correctly receives a message sent
 * by a client connected to Server A, with no direct connection between
 * the server instances themselves — Redis is the only shared dependency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageRelay implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat:room:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish a message to Redis so ALL instances (including this one)
     * relay it to their locally-connected subscribers.
     */
    public void publish(String roomId, ChatMessage chatMessage) {
        try {
            String channel = CHANNEL_PREFIX + roomId;
            String json = objectMapper.writeValueAsString(chatMessage);
            redisTemplate.convertAndSend(channel, json);
            log.debug("Published message to Redis channel={}", channel);
        } catch (Exception e) {
            log.error("Failed to publish message to Redis: {}", e.getMessage());
        }
    }

    /**
     * Called by Spring's Redis listener container whenever a message
     * arrives on a subscribed channel (from ANY instance, including self).
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String roomId = channel.substring(CHANNEL_PREFIX.length());
            String json = new String(message.getBody());

            ChatMessage chatMessage = objectMapper.readValue(
                json.replaceAll("^\"|\"$", ""), ChatMessage.class);

            // Deliver to all LOCAL WebSocket subscribers of this room's topic
            messagingTemplate.convertAndSend("/topic/room." + roomId, chatMessage);
            log.debug("Relayed message from Redis to local subscribers, room={}", roomId);
        } catch (Exception e) {
            log.error("Failed to process Redis pub/sub message: {}", e.getMessage());
        }
    }
}
