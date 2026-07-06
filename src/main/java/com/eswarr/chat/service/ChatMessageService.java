package com.eswarr.chat.service;

import com.eswarr.chat.model.ChatMessage;
import com.eswarr.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

/**
 * Handles persisting and broadcasting chat messages.
 * Every message is: 1) saved to Postgres, 2) published to Redis for
 * cross-instance delivery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final RedisMessageRelay redisMessageRelay;

    public ChatMessage sendMessage(String roomId, String senderId, String senderName, String content) {
        ChatMessage message = ChatMessage.builder()
            .roomId(roomId)
            .senderId(senderId)
            .senderName(senderName)
            .content(content)
            .type(ChatMessage.MessageType.CHAT)
            .build();

        ChatMessage saved = messageRepository.save(message);
        redisMessageRelay.publish(roomId, saved);

        log.debug("Message sent by {} in room {}", senderName, roomId);
        return saved;
    }

    public void broadcastSystemEvent(String roomId, String userId, String userName,
                                      ChatMessage.MessageType type) {
        ChatMessage event = ChatMessage.builder()
            .roomId(roomId)
            .senderId(userId)
            .senderName(userName)
            .content(userName + (type == ChatMessage.MessageType.JOIN ? " joined" : " left") + " the room")
            .type(type)
            .build();

        // System events (join/leave) are broadcast but not persisted
        redisMessageRelay.publish(roomId, event);
    }

    public List<ChatMessage> getRecentMessages(String roomId) {
        return messageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId);
    }

    public List<ChatMessage> getMessagesBefore(String roomId, Instant before, int limit) {
        return messageRepository.findByRoomIdBefore(roomId, before, PageRequest.of(0, limit));
    }
}
