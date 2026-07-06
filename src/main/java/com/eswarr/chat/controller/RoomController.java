package com.eswarr.chat.controller;

import com.eswarr.chat.model.ChatMessage;
import com.eswarr.chat.model.ChatRoom;
import com.eswarr.chat.model.UserPresence;
import com.eswarr.chat.repository.ChatRoomRepository;
import com.eswarr.chat.service.ChatMessageService;
import com.eswarr.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST API for room management and message history (non-realtime concerns).
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageService messageService;
    private final PresenceService presenceService;

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody Map<String, String> body) {
        ChatRoom room = ChatRoom.builder()
            .name(body.get("name"))
            .description(body.getOrDefault("description", ""))
            .createdBy(body.getOrDefault("createdBy", "anonymous"))
            .build();
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> listPublicRooms() {
        return ResponseEntity.ok(roomRepository.findByIsPrivateFalse());
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getRecentMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(messageService.getRecentMessages(roomId));
    }

    @GetMapping("/{roomId}/messages/before")
    public ResponseEntity<List<ChatMessage>> getMessagesBefore(
            @PathVariable String roomId,
            @RequestParam String before,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(
            messageService.getMessagesBefore(roomId, Instant.parse(before), limit));
    }

    @GetMapping("/{roomId}/presence")
    public ResponseEntity<List<UserPresence>> getOnlineUsers(@PathVariable String roomId) {
        return ResponseEntity.ok(presenceService.getOnlineUsers(roomId));
    }
}
