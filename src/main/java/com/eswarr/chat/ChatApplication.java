package com.eswarr.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Real-Time Chat System
 *
 * WebSocket + STOMP chat with Redis pub/sub for horizontal scaling
 * across multiple server instances. Supports rooms, presence tracking,
 * and persistent message history.
 *
 * @author Eswarr Dasi
 */
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
