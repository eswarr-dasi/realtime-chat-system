package com.eswarr.chat.repository;

import com.eswarr.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findByName(String name);
    List<ChatRoom> findByIsPrivateFalse();
}
