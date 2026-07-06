package com.eswarr.chat.service;

import com.eswarr.chat.model.UserPresence;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tracks which users are online, in which room, using Redis as the store.
 * Presence is ephemeral and high-frequency, so it lives in Redis (not Postgres)
 * with a short TTL — if a client disconnects without a clean LEAVE event,
 * the presence entry simply expires.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "presence:room:";
    private static final Duration PRESENCE_TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void markOnline(String roomId, String userId, String userName, String instanceId) {
        UserPresence presence = UserPresence.builder()
            .userId(userId)
            .userName(userName)
            .roomId(roomId)
            .status(UserPresence.Status.ONLINE)
            .lastSeenAt(Instant.now())
            .serverInstanceId(instanceId)
            .build();

        writePresence(roomId, userId, presence);
    }

    public void markTyping(String roomId, String userId, String userName, String instanceId) {
        UserPresence presence = UserPresence.builder()
            .userId(userId)
            .userName(userName)
            .roomId(roomId)
            .status(UserPresence.Status.TYPING)
            .lastSeenAt(Instant.now())
            .serverInstanceId(instanceId)
            .build();

        writePresence(roomId, userId, presence);
    }

    public void markOffline(String roomId, String userId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        redisTemplate.opsForHash().delete(key, userId);
        log.debug("User {} marked offline in room {}", userId, roomId);
    }

    /** Heartbeat — refresh TTL so the presence entry doesn't expire while active */
    public void heartbeat(String roomId, String userId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        redisTemplate.expire(key, PRESENCE_TTL);
    }

    public List<UserPresence> getOnlineUsers(String roomId) {
        String key = PRESENCE_KEY_PREFIX + roomId;
        Set<Object> values = redisTemplate.opsForHash().values(key).stream().collect(Collectors.toSet());

        return values.stream()
            .map(v -> {
                try {
                    return objectMapper.readValue((String) v, UserPresence.class);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(p -> p != null && !p.isStale(PRESENCE_TTL.getSeconds()))
            .collect(Collectors.toList());
    }

    private void writePresence(String roomId, String userId, UserPresence presence) {
        try {
            String key = PRESENCE_KEY_PREFIX + roomId;
            String json = objectMapper.writeValueAsString(presence);
            redisTemplate.opsForHash().put(key, userId, json);
            redisTemplate.expire(key, PRESENCE_TTL);
        } catch (Exception e) {
            log.error("Failed to write presence: {}", e.getMessage());
        }
    }
}
