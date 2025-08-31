package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.dto.ChatDTO;
import com.pbs.app.dto.MessageDTO;
import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.MessageRepository;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.services.JWTService;
import com.pbs.app.enums.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JWTService jwtService;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private ObjectMapper objectMapper;

    private User testUserA;
    private User testUserB;
    private Chat testChat;

    @BeforeEach
    void setUp() throws Exception {
        // Konfiguracja WebSocket klienta
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        objectMapper = new ObjectMapper();

        // Mock JWT service for test endpoint
        when(jwtService.extractUserId(any(String.class))).thenReturn("1");

        // Połączenie z WebSocket - używamy endpointu testowego
        String url = "ws://localhost:" + port + "/api";
        stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Wyczyszczenie bazy danych
        messageRepository.deleteAll();
        chatRepository.deleteAll();
        userRepository.deleteAll();

        // Przygotowanie danych testowych
        testUserA = User.builder()
                .firstName("Jan")
                .surname("Kowalski")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .groups(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        testUserA = userRepository.save(testUserA);

        testUserB = User.builder()
                .firstName("Anna")
                .surname("Nowak")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .groups(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        testUserB = userRepository.save(testUserB);

        testChat = Chat.builder()
                .userA(testUserA)
                .userB(testUserB)
                .createdAt(Instant.now())
                .build();
        testChat = chatRepository.save(testChat);
    }

    @Test
    void shouldSendAndReceiveMessage() throws Exception {
        // Given
        BlockingQueue<MessageDTO> messageQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to user queue
        stompSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((MessageDTO) payload);
            }
        });

        // Prepare message
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatId(testChat.getId());
        messageDTO.setSenderId(testUserA.getId());
        messageDTO.setContent("Test WebSocket message");

        // When
        stompSession.send("/app/chat.send", messageDTO);

        // Then
        MessageDTO receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals(testChat.getId(), receivedMessage.getChatId());
        assertEquals(testUserA.getId(), receivedMessage.getSenderId());
        assertEquals("Test WebSocket message", receivedMessage.getContent());
        assertNotNull(receivedMessage.getSentAt());

        // Verify message is saved in database
        List<Message> messages = messageRepository.findByChatIdOrderBySentAtAsc(testChat.getId());
        assertEquals(1, messages.size());
        assertEquals("Test WebSocket message", messages.get(0).getContent());
    }

    @Test
    void shouldReceiveMessageHistory() throws Exception {
        // Given
        // Save some messages in the database
        Message message1 = Message.builder()
                .chat(testChat)
                .sender(testUserA)
                .content("First message")
                .sentAt(Instant.now().minusSeconds(10))
                .build();
        messageRepository.save(message1);

        Message message2 = Message.builder()
                .chat(testChat)
                .sender(testUserB)
                .content("Second message")
                .sentAt(Instant.now().minusSeconds(5))
                .build();
        messageRepository.save(message2);

        BlockingQueue<List<MessageDTO>> historyQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to history queue
        stompSession.subscribe("/user/queue/history", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                historyQueue.offer((List<MessageDTO>) payload);
            }
        });

        // Request history
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(testChat.getId());

        // When
        stompSession.send("/app/chat.history", chatDTO);

        // Then
        List<MessageDTO> history = historyQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(history);
        assertEquals(2, history.size());
        
        // Verify messages are in correct order (ascending by sentAt)
        assertEquals("First message", history.get(0).getContent());
        assertEquals("Second message", history.get(1).getContent());
        assertEquals(testUserA.getId(), history.get(0).getSenderId());
        assertEquals(testUserB.getId(), history.get(1).getSenderId());
    }

    @Test
    void shouldHandleEmptyMessageHistory() throws Exception {
        // Given
        BlockingQueue<List<MessageDTO>> historyQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to history queue
        stompSession.subscribe("/user/queue/history", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                historyQueue.offer((List<MessageDTO>) payload);
            }
        });

        // Request history for chat with no messages
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(testChat.getId());

        // When
        stompSession.send("/app/chat.history", chatDTO);

        // Then
        List<MessageDTO> history = historyQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldHandleMultipleMessagesInSequence() throws Exception {
        // Given
        BlockingQueue<MessageDTO> messageQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to user queue
        stompSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((MessageDTO) payload);
            }
        });

        // Send multiple messages
        for (int i = 1; i <= 3; i++) {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setChatId(testChat.getId());
            messageDTO.setSenderId(testUserA.getId());
            messageDTO.setContent("Message " + i);

            stompSession.send("/app/chat.send", messageDTO);
        }

        // Then
        for (int i = 1; i <= 3; i++) {
            MessageDTO receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertNotNull(receivedMessage);
            assertEquals("Message " + i, receivedMessage.getContent());
        }

        // Verify all messages are saved in database
        List<Message> messages = messageRepository.findByChatIdOrderBySentAtAsc(testChat.getId());
        assertEquals(3, messages.size());
    }

    @Test
    void shouldHandleEmptyMessageContent() throws Exception {
        // Given
        BlockingQueue<MessageDTO> messageQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to user queue
        stompSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((MessageDTO) payload);
            }
        });

        // Send message with empty content
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatId(testChat.getId());
        messageDTO.setSenderId(testUserA.getId());
        messageDTO.setContent("");

        // When
        stompSession.send("/app/chat.send", messageDTO);

        // Then
        MessageDTO receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals("", receivedMessage.getContent());
    }

    @Test
    void shouldHandleLongMessageContent() throws Exception {
        // Given
        BlockingQueue<MessageDTO> messageQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to user queue
        stompSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((MessageDTO) payload);
            }
        });

        // Send message with long content
        String longContent = "A".repeat(1000);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatId(testChat.getId());
        messageDTO.setSenderId(testUserA.getId());
        messageDTO.setContent(longContent);

        // When
        stompSession.send("/app/chat.send", messageDTO);

        // Then
        MessageDTO receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);
        assertEquals(longContent, receivedMessage.getContent());
    }

    @Test
    void shouldMaintainMessageOrder() throws Exception {
        // Given
        BlockingQueue<MessageDTO> messageQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to user queue
        stompSession.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((MessageDTO) payload);
            }
        });

        // Send messages with slight delays to ensure order
        String[] messages = {"First", "Second", "Third", "Fourth", "Fifth"};
        for (String content : messages) {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setChatId(testChat.getId());
            messageDTO.setSenderId(testUserA.getId());
            messageDTO.setContent(content);

            stompSession.send("/app/chat.send", messageDTO);
            Thread.sleep(100); // Small delay to ensure order
        }

        // Then verify messages are received in correct order
        for (String expectedContent : messages) {
            MessageDTO receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertNotNull(receivedMessage);
            assertEquals(expectedContent, receivedMessage.getContent());
        }
    }

    @Test
    void shouldHandleHistoryForNonExistentChat() throws Exception {
        // Given
        BlockingQueue<List<MessageDTO>> historyQueue = new LinkedBlockingQueue<>();
        
        // Subscribe to history queue
        stompSession.subscribe("/user/queue/history", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                historyQueue.offer((List<MessageDTO>) payload);
            }
        });

        // Request history for non-existent chat
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setId(999L);

        // When
        stompSession.send("/app/chat.history", chatDTO);

        // Then
        List<MessageDTO> history = historyQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}