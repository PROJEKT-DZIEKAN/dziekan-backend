package com.pbs.app.controllers;

import com.pbs.app.dto.ChatDTO;
import com.pbs.app.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatDTO> listChats(@RequestParam Long userId) {
        return chatService.findByUser(userId).stream()
            .map(c -> ChatDTO.builder()
                .id(c.getId())
                .userAId(c.getUserA().getId())
                .userBId(c.getUserB().getId())
                .build())
            .toList();
    }

    @PostMapping("/get-or-create")
    public ChatDTO getOrCreate(@RequestBody ChatDTO req) {
        var chat = chatService.getOrCreateChat(req.getUserAId(), req.getUserBId());
        return ChatDTO.builder()
            .id(chat.getId())
            .userAId(chat.getUserA().getId())
            .userBId(chat.getUserB().getId())
            .build();
    }
}
