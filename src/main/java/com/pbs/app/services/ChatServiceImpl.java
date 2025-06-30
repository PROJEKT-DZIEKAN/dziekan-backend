package com.pbs.app.services;

import com.pbs.app.models.Chat;
import com.pbs.app.models.User;
import com.pbs.app.models.Message;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    public ChatServiceImpl(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Chat createChat(Chat chat) {
        chat.setId(null); // Ensure the ID is null to use the generator
        return chatRepository.save(chat);
    }

    @Override
    @Transactional
    public Chat updateChat(Long id, Chat chat) {
        if (!chatRepository.existsById(id)) {
            throw new EntityNotFoundException("Chat with id " + id + " does not exist.");
        }
        chat.setId(id); // Set the ID to ensure the correct entity is updated
        return chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void deleteChat(Long id) {
        if (!chatRepository.existsById(id)) {
            throw new EntityNotFoundException("Chat with id " + id + " does not exist.");
        }
        chatRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Chat getChatById(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat with id " + id + " does not exist."));
    }

    @Override
    @Transactional
    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    @Override
    @Transactional
    public List<Chat> findByUserA(Long userAId) {
        User userA = userRepository.findById(userAId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userAId + " does not exist."));
        return chatRepository.findByUserA(userA);
    }

    @Override
    @Transactional
    public List<Chat> findByUserB(Long userBId) {
        User userB = userRepository.findById(userBId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userBId + " does not exist."));
        return chatRepository.findByUserB(userB);
    }

    @Override
    @Transactional
    public List<Chat> findByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " does not exist."));
        return chatRepository.findByUser(user);
    }

    @Override
    @Transactional
    public Optional<Chat> findByUserAAndUserB(Long userAId, Long userBId) {
        User userA = userRepository.findById(userAId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userAId + " does not exist."));
        User userB = userRepository.findById(userBId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userBId + " does not exist."));
        return chatRepository.findByUserAAndUserB(userA, userB);
    }

    @Override
    @Transactional
    public boolean existsByUserAAndUserB(Long userAId, Long userBId) {
        User userA = userRepository.findById(userAId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userAId + " does not exist."));
        User userB = userRepository.findById(userBId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userBId + " does not exist."));
        return chatRepository.existsByUserAAndUserB(userA, userB);
    }

    @Override
    @Transactional
    public int countMessagesInChat(Long chatId) {
        return chatRepository.countMessagesInChat(chatId);
    }

    @Override
    @Transactional
    public Optional<Message> getLastMessage(Long chatId) {
        return chatRepository.getLastMessage(chatId);
    }
}
