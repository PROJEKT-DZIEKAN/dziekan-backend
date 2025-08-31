package com.pbs.app;

import com.pbs.app.controllers.ChatWebSocketController;
import com.pbs.app.dto.ChatDTO;
import com.pbs.app.dto.MessageDTO;
import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import com.pbs.app.services.ChatService;
import com.pbs.app.enums.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerUnitTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private Principal principal;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private User testUserA;
    private User testUserB;
    private Chat testChat;
    private Message testMessage;
    private MessageDTO testMessageDTO;

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

        testMessageDTO = new MessageDTO();
        testMessageDTO.setChatId(1L);
        testMessageDTO.setSenderId(1L);
        testMessageDTO.setContent("Test message");
        testMessageDTO.setSentAt(Instant.now());
    }

    @Test
    void sendMessage_Success() throws Exception {
        // Given
        when(principal.getName()).thenReturn("testUser");
        when(chatService.saveMessage(1L, 1L, "Test message")).thenReturn(testMessage);

        // When
        chatWebSocketController.sendMessage(testMessageDTO, principal);

        // Then
        verify(chatService).saveMessage(1L, 1L, "Test message");
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/messages"), any(MessageDTO.class));
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    void sendMessage_WithoutPrincipal() throws Exception {
        // Given
        when(chatService.saveMessage(1L, 1L, "Test message")).thenReturn(testMessage);

        // When
        chatWebSocketController.sendMessage(testMessageDTO, null);

        // Then
        verify(chatService).saveMessage(1L, 1L, "Test message");
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/messages"), any(MessageDTO.class));
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    void sendMessage_ServiceException() throws Exception {
        // Given
        when(principal.getName()).thenReturn("testUser");
        when(chatService.saveMessage(1L, 1L, "Test message"))
                .thenThrow(new RuntimeException("Chat not found"));

        // When & Then
        try {
            chatWebSocketController.sendMessage(testMessageDTO, principal);
        } catch (RuntimeException e) {
            // Exception should be thrown
        }

        verify(chatService).saveMessage(1L, 1L, "Test message");
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void sendMessage_EmptyContent() throws Exception {
        // Given
        MessageDTO emptyMessageDTO = new MessageDTO();
        emptyMessageDTO.setChatId(1L);
        emptyMessageDTO.setSenderId(1L);
        emptyMessageDTO.setContent("");

        Message emptyMessage = Message.builder()
                .id(1L)
                .chat(testChat)
                .sender(testUserA)
                .content("")
                .sentAt(Instant.now())
                .build();

        when(principal.getName()).thenReturn("testUser");
        when(chatService.saveMessage(1L, 1L, "")).thenReturn(emptyMessage);

        // When
        chatWebSocketController.sendMessage(emptyMessageDTO, principal);

        // Then
        verify(chatService).saveMessage(1L, 1L, "");
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/messages"), any(MessageDTO.class));
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    void sendMessage_LongContent() throws Exception {
        // Given
        String longContent = "A".repeat(1000);
        MessageDTO longMessageDTO = new MessageDTO();
        longMessageDTO.setChatId(1L);
        longMessageDTO.setSenderId(1L);
        longMessageDTO.setContent(longContent);

        Message longMessage = Message.builder()
                .id(1L)
                .chat(testChat)
                .sender(testUserA)
                .content(longContent)
                .sentAt(Instant.now())
                .build();

        when(principal.getName()).thenReturn("testUser");
        when(chatService.saveMessage(1L, 1L, longContent)).thenReturn(longMessage);

        // When
        chatWebSocketController.sendMessage(longMessageDTO, principal);

        // Then
        verify(chatService).saveMessage(1L, 1L, longContent);
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/messages"), any(MessageDTO.class));
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    void history_Success() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        List<Message> messages = Arrays.asList(testMessage);
        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(1L)).thenReturn(messages);

        // When
        chatWebSocketController.history(chatDTO, principal);

        // Then
        verify(chatService).getHistory(1L);
        verify(messagingTemplate).convertAndSendToUser(eq("testUser"), eq("/queue/history"), any(List.class));
    }

    @Test
    void history_EmptyHistory() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(1L)).thenReturn(Arrays.asList());

        // When
        chatWebSocketController.history(chatDTO, principal);

        // Then
        verify(chatService).getHistory(1L);
        verify(messagingTemplate).convertAndSendToUser(eq("testUser"), eq("/queue/history"), any(List.class));
    }

    @Test
    void history_MultipleMessages() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        Message message2 = Message.builder()
                .id(2L)
                .chat(testChat)
                .sender(testUserB)
                .content("Second message")
                .sentAt(Instant.now())
                .build();

        List<Message> messages = Arrays.asList(testMessage, message2);
        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(1L)).thenReturn(messages);

        // When
        chatWebSocketController.history(chatDTO, principal);

        // Then
        verify(chatService).getHistory(1L);
        verify(messagingTemplate).convertAndSendToUser(eq("testUser"), eq("/queue/history"), any(List.class));
    }

    @Test
    void history_WithoutPrincipal() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        // When
        chatWebSocketController.history(chatDTO, null);

        // Then
        verify(chatService, never()).getHistory(anyLong());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void history_ServiceException() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(1L)).thenThrow(new RuntimeException("Chat not found"));

        // When & Then
        try {
            chatWebSocketController.history(chatDTO, principal);
        } catch (RuntimeException e) {
            // Exception should be thrown
        }

        verify(chatService).getHistory(1L);
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void history_NullChatId() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(null);

        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(null)).thenReturn(Arrays.asList()); // Mock zwraca pustą listę

        // When
        chatWebSocketController.history(chatDTO, principal);

        // Then
        verify(chatService).getHistory(null);
        verify(messagingTemplate).convertAndSendToUser(eq("testUser"), eq("/queue/history"), eq(Arrays.asList()));
    }

    @Test
    void sendMessage_VerifyMessageDTOMapping() throws Exception {
        // Given
        when(principal.getName()).thenReturn("testUser");
        when(chatService.saveMessage(1L, 1L, "Test message")).thenReturn(testMessage);

        // When
        chatWebSocketController.sendMessage(testMessageDTO, principal);

        // Then
        verify(messagingTemplate).convertAndSendToUser(eq("1"), eq("/queue/messages"), argThat(messageDTO -> {
            MessageDTO dto = (MessageDTO) messageDTO;
            return dto.getChatId().equals(1L) && 
                   dto.getSenderId().equals(1L) && 
                   dto.getContent().equals("Test message");
        }));
    }

    @Test
    void history_VerifyMessageDTOListMapping() throws Exception {
        // Given
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(1L);

        Message message2 = Message.builder()
                .id(2L)
                .chat(testChat)
                .sender(testUserB)
                .content("Second message")
                .sentAt(Instant.now())
                .build();

        List<Message> messages = Arrays.asList(testMessage, message2);
        when(principal.getName()).thenReturn("testUser");
        when(chatService.getHistory(1L)).thenReturn(messages);

        // When
        chatWebSocketController.history(chatDTO, principal);

        // Then
        verify(messagingTemplate).convertAndSendToUser(eq("testUser"), eq("/queue/history"), argThat(messageList -> {
            List<MessageDTO> dtos = (List<MessageDTO>) messageList;
            return dtos.size() == 2 && 
                   dtos.get(0).getContent().equals("Test message") && 
                   dtos.get(1).getContent().equals("Second message");
        }));
    }
}