package com.pbs.app.controllers;

import com.pbs.app.dto.ChatDTO;
import com.pbs.app.dto.MessageDTO;
import com.pbs.app.models.Message;
import com.pbs.app.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final Logger log =
            LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO dto, Principal principal) {

        String user = principal != null ? principal.getName() : "unknown";
        log.info("sendMessage by {}: {}", user, dto);

        Message saved = chatService
                .saveMessage(dto.getChatId(), dto.getSenderId(), dto.getContent());

        MessageDTO resp = new MessageDTO();
        resp.setChatId(saved.getChat().getId());
        resp.setSenderId(saved.getSender().getId());
        resp.setContent(saved.getContent());
        resp.setSentAt(saved.getSentAt());

        String a = saved.getChat().getUserA().getId().toString();
        String b = saved.getChat().getUserB().getId().toString();

        messagingTemplate.convertAndSendToUser(a, "/queue/messages", resp);
        messagingTemplate.convertAndSendToUser(b, "/queue/messages", resp);
    }

    @MessageMapping("/chat.history")
    public void history(@Payload ChatDTO dto, Principal principal) {

        if (principal == null) {
            log.warn("history: no principal, skipping");
            return;
        }
        String user = principal.getName();
        log.info("history by {} for chat {}", user, dto.getId());

        List<MessageDTO> hist = chatService.getHistory(dto.getId()).stream()
                .map(m -> {
                    MessageDTO md = new MessageDTO();
                    md.setChatId(m.getChat().getId());
                    md.setSenderId(m.getSender().getId());
                    md.setContent(m.getContent());
                    md.setSentAt(m.getSentAt());
                    return md;
                })
                .toList();

        messagingTemplate.convertAndSendToUser(user, "/queue/history", hist);
    }
}