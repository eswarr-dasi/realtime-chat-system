package com.eswarr.chat.repository;

import com.eswarr.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * Cursor-based pagination: fetch messages older than a given timestamp.
     * More efficient than offset-based pagination for chat history.
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.createdAt < :before " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessage> findByRoomIdBefore(
        @Param("roomId") String roomId, @Param("before") Instant before, Pageable pageable);

    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(String roomId);

    long countByRoomId(String roomId);
}
