package com.eswarr.chat.config;

import com.eswarr.chat.service.RedisMessageRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures Redis for both:
 *  1. Pub/Sub message relay (cross-instance chat broadcast)
 *  2. General key-value storage (presence tracking)
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Subscribes this instance to ALL chat room channels via pattern matching.
     * "chat:room:*" matches "chat:room:general", "chat:room:engineering", etc.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessageRelay redisMessageRelay) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisMessageRelay, new PatternTopic("chat:room:*"));
        return container;
    }
}
