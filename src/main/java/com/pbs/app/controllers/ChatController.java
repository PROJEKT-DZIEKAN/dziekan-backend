package com.pbs.app.controllers;


import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.services.ChatServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatServiceImpl chatService;

    @PostMapping("/create")
    public ResponseEntity<Chat> createChat(@Valid @RequestBody Chat chat)
    {
        Chat createdChat = chatService.createChat(chat);
        return ResponseEntity.ok(createdChat);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Chat> updateChat(@PathVariable Long id, @Valid @RequestBody Chat chat) {
        try {
            Chat updatedChat = chatService.updateChat(id, chat);
            return ResponseEntity.ok(updatedChat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        try {
            chatService.deleteChat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chat> getChatById(@PathVariable Long id) {
        try {
            Chat chat = chatService.getChatById(id);
            return ResponseEntity.ok(chat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Chat>> getAllChats() {
        List<Chat> chats = chatService.getAllChats();
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/user-a/{userAId}")
    public ResponseEntity<List<Chat>> findByUserA(@PathVariable Long userAId) {
        List<Chat> chats = chatService.findByUserA(userAId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/user-b/{userBId}")
    public ResponseEntity<List<Chat>> findByUserB(@PathVariable Long userBId) {
        List<Chat> chats = chatService.findByUserB(userBId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Chat>> findByUser(@PathVariable Long userId) {
        List<Chat> chats = chatService.findByUser(userId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/by-users")
    public ResponseEntity<Chat> findByUserAAndUserB(@RequestParam Long userAId, @RequestParam Long userBId) {
        return chatService.findByUserAAndUserB(userAId, userBId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByUserAAndUserB(@RequestParam Long userAId, @RequestParam Long userBId) {
        boolean exists = chatService.existsByUserAAndUserB(userAId, userBId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/message-count/{chatId}")
    public ResponseEntity<Integer> countMessagesInChat(@PathVariable Long chatId) {
        int messageCount = chatService.countMessagesInChat(chatId);
        return ResponseEntity.ok(messageCount);
    }

    @GetMapping("/last-message/{chatId}")
    public ResponseEntity<Message> getLastMessage(@PathVariable Long chatId) {
        return chatService.getLastMessage(chatId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
