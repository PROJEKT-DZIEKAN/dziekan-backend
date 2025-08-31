package com.pbs.app.controllers;

import com.pbs.app.dto.ChatDTO;
import com.pbs.app.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

        if (req.getUserAId() == null || req.getUserBId() == null) {
            throw new IllegalArgumentException("Both userAId and userBId must be provided");
        }
        var chat = chatService.getOrCreateChat(req.getUserAId(), req.getUserBId());

        if (chat == null) {
            throw new IllegalStateException("Failed to create or retrieve chat");
        }

        return ChatDTO.builder()
            .id(chat.getId())
            .userAId(chat.getUserA().getId())
            .userBId(chat.getUserB().getId())
            .build();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleIllegalArgumentException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }


}
