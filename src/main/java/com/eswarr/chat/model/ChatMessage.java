package com.eswarr.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * A single chat message, persisted for history/pagination
 * and broadcast live via WebSocket + Redis pub/sub.
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_room_created", columnList = "roomId,createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (type == null) type = MessageType.CHAT;
    }

    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING
    }
}
