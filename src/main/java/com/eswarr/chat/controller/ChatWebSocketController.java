package com.eswarr.chat.controller;

import com.eswarr.chat.model.ChatMessage;
import com.eswarr.chat.service.ChatMessageService;
import com.eswarr.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;

/**
 * Handles incoming STOMP messages from WebSocket clients.
 *
 * Client sends to /app/chat.send.{roomId} -> handled here -> persisted +
 * relayed via Redis -> delivered to /topic/room.{roomId} subscribers.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final PresenceService presenceService;

    @Value("${chat.instance-id:instance-1}")
    private String instanceId;

    @MessageMapping("/chat.send.{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                             Map<String, String> payload,
                             SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");
        String content = payload.get("content");

        chatMessageService.sendMessage(roomId, userId, userName, content);
    }

    @MessageMapping("/chat.join.{roomId}")
    public void joinRoom(@DestinationVariable String roomId,
                          SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");

        presenceService.markOnline(roomId, userId, userName, instanceId);
        chatMessageService.broadcastSystemEvent(roomId, userId, userName, ChatMessage.MessageType.JOIN);
        log.info("User {} joined room {}", userName, roomId);
    }

    @MessageMapping("/chat.leave.{roomId}")
    public void leaveRoom(@DestinationVariable String roomId,
                           SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");

        presenceService.markOffline(roomId, userId);
        chatMessageService.broadcastSystemEvent(roomId, userId, userName, ChatMessage.MessageType.LEAVE);
        log.info("User {} left room {}", userName, roomId);
    }

    @MessageMapping("/chat.typing.{roomId}")
    public void typing(@DestinationVariable String roomId,
                        SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");

        presenceService.markTyping(roomId, userId, userName, instanceId);
    }

    @MessageMapping("/chat.heartbeat.{roomId}")
    public void heartbeat(@DestinationVariable String roomId,
                           SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        presenceService.heartbeat(roomId, userId);
    }
}
