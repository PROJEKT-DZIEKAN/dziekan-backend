package com.pbs.app.services;

import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.MessageRepository;
import com.pbs.app.repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {
    private final ChatRepository chatRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo;

    public ChatService(
        ChatRepository chatRepo,
        MessageRepository msgRepo,
        UserRepository userRepo
    ) {
        this.chatRepo = chatRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
    }

    /** Zapisuje nową wiadomość do istniejącego czatu. */
    @Transactional
    public Message saveMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepo.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));
        User sender = userRepo.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + senderId));

        Message msg = Message.builder()
            .chat(chat)
            .sender(sender)
            .content(content)
            .build();

        return msgRepo.save(msg);
    }

    /** Zwraca historię wiadomości w danym czacie, posortowaną rosnąco po czasie. */
    public List<Message> getHistory(Long chatId) {
        return msgRepo.findByChatIdOrderBySentAtAsc(chatId);
    }

    /**
     * Zwraca istniejący czat między userA i userB lub tworzy nowy.
     * Używamy findById zamiast getOne, by natychmiast zweryfikować istnienie użytkownika.
     */
    @Transactional
    public Chat getOrCreateChat(Long userAId, Long userBId) {
        // Pobranie encji userów lub błąd, jeśli nie istnieją
        User userA = userRepo.findById(userAId)
            .orElseThrow(() -> new IllegalArgumentException("User A not found: " + userAId));
        User userB = userRepo.findById(userBId)
            .orElseThrow(() -> new IllegalArgumentException("User B not found: " + userBId));

        // Szukamy czatu w obu konfiguracjach A–B lub B–A
        return chatRepo.findByUserA_IdAndUserB_Id(userAId, userBId)
            .or(() -> chatRepo.findByUserA_IdAndUserB_Id(userBId, userAId))
            .orElseGet(() -> {
                // Budujemy czat (pole createdAt poprawnie wypełnila wartość domyślna z @Builder.Default)
                Chat c = Chat.builder()
                    .userA(userA)
                    .userB(userB)
                    .build();
                return chatRepo.save(c);
            });
    }

    /** Zwraca listę wszystkich czatów, w których bierze udział dany użytkownik. */
    public List<Chat> findByUser(Long userId) {
        return chatRepo.findByUserA_IdOrUserB_Id(userId, userId);
    }
}
