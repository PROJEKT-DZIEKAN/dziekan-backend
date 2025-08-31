package com.pbs.app;

import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.MessageRepository;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.services.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceUnitTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User testUserA;
    private User testUserB;
    private Chat testChat;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testUserA = User.builder()
                .id(1L)
                .firstName("Jan")
                .surname("Kowalski")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .build();

        testUserB = User.builder()
                .id(2L)
                .firstName("Anna")
                .surname("Nowak")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .build();

        testChat = Chat.builder()
                .id(1L)
                .userA(testUserA)
                .userB(testUserB)
                .createdAt(Instant.now())
                .build();

        testMessage = Message.builder()
                .id(1L)
                .chat(testChat)
                .sender(testUserA)
                .content("Test message")
                .sentAt(Instant.now())
                .build();
    }

    @Test
    void saveMessage_Success() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Message result = chatService.saveMessage(1L, 1L, "Test message");

        // Then
        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        assertEquals(testUserA, result.getSender());
        assertEquals(testChat, result.getChat());
        verify(chatRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void saveMessage_ChatNotFound() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatService.saveMessage(1L, 1L, "Test message"));

        assertEquals("Chat not found: 1", exception.getMessage());
        verify(chatRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessage_UserNotFound() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatService.saveMessage(1L, 1L, "Test message"));

        assertEquals("User not found: 1", exception.getMessage());
        verify(chatRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void saveMessage_EmptyContent() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        
        Message emptyMessage = Message.builder()
                .id(1L)
                .chat(testChat)
                .sender(testUserA)
                .content("")
                .sentAt(Instant.now())
                .build();
        
        when(messageRepository.save(any(Message.class))).thenReturn(emptyMessage);

        // When
        Message result = chatService.saveMessage(1L, 1L, "");

        // Then
        assertNotNull(result);
        assertEquals("", result.getContent());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void saveMessage_NullContent() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        
        Message nullMessage = Message.builder()
                .id(1L)
                .chat(testChat)
                .sender(testUserA)
                .content(null)
                .sentAt(Instant.now())
                .build();
        
        when(messageRepository.save(any(Message.class))).thenReturn(nullMessage);

        // When
        Message result = chatService.saveMessage(1L, 1L, null);

        // Then
        assertNotNull(result);
        assertNull(result.getContent());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getHistory_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByChatIdOrderBySentAtAsc(1L)).thenReturn(messages);

        // When
        List<Message> result = chatService.getHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage, result.get(0));
        verify(messageRepository).findByChatIdOrderBySentAtAsc(1L);
    }

    @Test
    void getHistory_EmptyHistory() {
        // Given
        when(messageRepository.findByChatIdOrderBySentAtAsc(1L)).thenReturn(Arrays.asList());

        // When
        List<Message> result = chatService.getHistory(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findByChatIdOrderBySentAtAsc(1L);
    }

    @Test
    void getHistory_MultipleMessages() {
        // Given
        Message message2 = Message.builder()
                .id(2L)
                .chat(testChat)
                .sender(testUserB)
                .content("Second message")
                .sentAt(Instant.now())
                .build();

        List<Message> messages = Arrays.asList(testMessage, message2);
        when(messageRepository.findByChatIdOrderBySentAtAsc(1L)).thenReturn(messages);

        // When
        List<Message> result = chatService.getHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testMessage, result.get(0));
        assertEquals(message2, result.get(1));
        verify(messageRepository).findByChatIdOrderBySentAtAsc(1L);
    }

    @Test
    void getOrCreateChat_CreateNew() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUserB));
        when(chatRepository.findByUserA_IdAndUserB_Id(1L, 2L)).thenReturn(Optional.empty());
        when(chatRepository.findByUserA_IdAndUserB_Id(2L, 1L)).thenReturn(Optional.empty());
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // When
        Chat result = chatService.getOrCreateChat(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(testChat, result);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(chatRepository).findByUserA_IdAndUserB_Id(1L, 2L);
        verify(chatRepository).findByUserA_IdAndUserB_Id(2L, 1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void getOrCreateChat_ReturnExisting() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUserB));
        when(chatRepository.findByUserA_IdAndUserB_Id(1L, 2L)).thenReturn(Optional.of(testChat));

        // When
        Chat result = chatService.getOrCreateChat(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(testChat, result);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(chatRepository).findByUserA_IdAndUserB_Id(1L, 2L);
        verify(chatRepository, never()).findByUserA_IdAndUserB_Id(2L, 1L);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void getOrCreateChat_ReturnExistingReversed() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUserB));
        when(chatRepository.findByUserA_IdAndUserB_Id(1L, 2L)).thenReturn(Optional.empty());
        when(chatRepository.findByUserA_IdAndUserB_Id(2L, 1L)).thenReturn(Optional.of(testChat));

        // When
        Chat result = chatService.getOrCreateChat(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(testChat, result);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(chatRepository).findByUserA_IdAndUserB_Id(1L, 2L);
        verify(chatRepository).findByUserA_IdAndUserB_Id(2L, 1L);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void getOrCreateChat_UserANotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatService.getOrCreateChat(1L, 2L));

        assertEquals("User A not found: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).findById(2L);
        verify(chatRepository, never()).findByUserA_IdAndUserB_Id(anyLong(), anyLong());
        verify(chatRepository, never()).save(any());
    }

    @Test
    void getOrCreateChat_UserBNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatService.getOrCreateChat(1L, 2L));

        assertEquals("User B not found: 2", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(chatRepository, never()).findByUserA_IdAndUserB_Id(anyLong(), anyLong());
        verify(chatRepository, never()).save(any());
    }

    @Test
    void getOrCreateChat_SameUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserA));
        when(chatRepository.findByUserA_IdAndUserB_Id(1L, 1L)).thenReturn(Optional.empty());
        
        Chat selfChat = Chat.builder()
                .id(2L)
                .userA(testUserA)
                .userB(testUserA)
                .createdAt(Instant.now())
                .build();
        
        when(chatRepository.save(any(Chat.class))).thenReturn(selfChat);

        // When
        Chat result = chatService.getOrCreateChat(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(selfChat, result);
        verify(userRepository, times(2)).findById(1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void findByUser_Success() {
        // Given
        List<Chat> chats = Arrays.asList(testChat);
        when(chatRepository.findByUserA_IdOrUserB_Id(1L, 1L)).thenReturn(chats);

        // When
        List<Chat> result = chatService.findByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChat, result.get(0));
        verify(chatRepository).findByUserA_IdOrUserB_Id(1L, 1L);
    }

    @Test
    void findByUser_EmptyList() {
        // Given
        when(chatRepository.findByUserA_IdOrUserB_Id(1L, 1L)).thenReturn(Arrays.asList());

        // When
        List<Chat> result = chatService.findByUser(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatRepository).findByUserA_IdOrUserB_Id(1L, 1L);
    }

    @Test
    void findByUser_MultipleChats() {
        // Given
        Chat chat2 = Chat.builder()
                .id(2L)
                .userA(testUserA)
                .userB(User.builder().id(3L).build())
                .createdAt(Instant.now())
                .build();

        List<Chat> chats = Arrays.asList(testChat, chat2);
        when(chatRepository.findByUserA_IdOrUserB_Id(1L, 1L)).thenReturn(chats);

        // When
        List<Chat> result = chatService.findByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testChat, result.get(0));
        assertEquals(chat2, result.get(1));
        verify(chatRepository).findByUserA_IdOrUserB_Id(1L, 1L);
    }
}