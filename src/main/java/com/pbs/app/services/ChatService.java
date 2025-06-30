package com.pbs.app.services;

import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;

import java.util.List;
import java.util.Optional;

public interface ChatService {
    Chat createChat(Chat chat);
    Chat updateChat(Long id, Chat chat);
    void deleteChat(Long id);
    Chat getChatById(Long id);
    List<Chat> getAllChats();
    List<Chat> findByUserA(Long userAId);
    List<Chat> findByUserB(Long userBId);
    List<Chat> findByUser(Long userId);
    Optional<Chat> findByUserAAndUserB(Long userAId, Long userBId);
    boolean existsByUserAAndUserB(Long userAId, Long userBId);
    int countMessagesInChat(Long chatId);
    Optional<Message> getLastMessage(Long chatId);
}
