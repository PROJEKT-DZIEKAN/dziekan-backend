package com.pbs.app.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notification.send")
    public void sendNotification(@Payload String message, Principal principal) {
        String userId = principal.getName();
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
    }

    @MessageMapping("/notification.broadcast")
    public void broadcastNotification(@Payload String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}
