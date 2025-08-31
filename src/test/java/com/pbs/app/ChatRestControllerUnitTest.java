package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.controllers.ChatRestController;
import com.pbs.app.dto.ChatDTO;
import com.pbs.app.models.Chat;
import com.pbs.app.models.User;
import com.pbs.app.services.ChatService;
import com.pbs.app.enums.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatRestControllerUnitTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatRestController chatRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUserA;
    private User testUserB;
    private User testUserC;
    private Chat testChat1;
    private Chat testChat2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatRestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

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

        testUserC = User.builder()
                .id(3L)
                .firstName("Piotr")
                .surname("Zieliński")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .build();

        testChat1 = Chat.builder()
                .id(1L)
                .userA(testUserA)
                .userB(testUserB)
                .createdAt(Instant.now())
                .build();

        testChat2 = Chat.builder()
                .id(2L)
                .userA(testUserA)
                .userB(testUserC)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void listChats_Success() throws Exception {
        // Given
        List<Chat> chats = Arrays.asList(testChat1);
        when(chatService.findByUser(1L)).thenReturn(chats);

        // When & Then
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userAId").value(1L))
                .andExpect(jsonPath("$[0].userBId").value(2L));

        verify(chatService).findByUser(1L);
    }

    @Test
    void listChats_EmptyList() throws Exception {
        // Given
        when(chatService.findByUser(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(chatService).findByUser(1L);
    }

    @Test
    void listChats_MultipleChats() throws Exception {
        // Given
        List<Chat> chats = Arrays.asList(testChat1, testChat2);
        when(chatService.findByUser(1L)).thenReturn(chats);

        // When & Then
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(chatService).findByUser(1L);
    }

//    @Test
//    void listChats_ServiceException() throws Exception {
//        // Given
//        when(chatService.findByUser(1L)).thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        mockMvc.perform(get("/api/chats")
//                        .param("userId", "1"))
//                .andExpect(result -> {
//                    int status = result.getResponse().getStatus();
//                    if (status < 400 || status >= 600) {
//                        throw new AssertionError("Expected 4xx or 5xx status but was: " + status);
//                    }
//                });
//
//        verify(chatService).findByUser(1L);
//    }

    @Test
    void listChats_MissingUserIdParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).findByUser(anyLong());
    }

    @Test
    void getOrCreateChat_Success() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(2L)
                .build();

        when(chatService.getOrCreateChat(1L, 2L)).thenReturn(testChat1);

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userAId").value(1L))
                .andExpect(jsonPath("$.userBId").value(2L));

        verify(chatService).getOrCreateChat(1L, 2L);
    }

    @Test
    void getOrCreateChat_CreateNewChat() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(3L)
                .build();

        when(chatService.getOrCreateChat(1L, 3L)).thenReturn(testChat2);

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.userAId").value(1L))
                .andExpect(jsonPath("$.userBId").value(3L));

        verify(chatService).getOrCreateChat(1L, 3L);
    }

    @Test
    void getOrCreateChat_UserNotFound() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(999L)
                .build();

        when(chatService.getOrCreateChat(1L, 999L))
                .thenThrow(new IllegalArgumentException("User B not found: 999"));

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status < 400 || status >= 600) {
                        throw new AssertionError("Expected 4xx or 5xx status but was: " + status);
                    }
                });

        verify(chatService).getOrCreateChat(1L, 999L);
    }

    @Test
    void getOrCreateChat_InvalidJson() throws Exception {
        // Given
        String invalidJson = "{invalid json}";

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).getOrCreateChat(anyLong(), anyLong());
    }

    @Test
    void getOrCreateChat_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).getOrCreateChat(anyLong(), anyLong());
    }

    @Test
    void getOrCreateChat_NullUserIds() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(null)
                .userBId(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status < 400 || status >= 600) {
                        throw new AssertionError("Expected 4xx or 5xx status but was: " + status);
                    }
                });

        verify(chatService, never()).getOrCreateChat(anyLong(), anyLong());
    }

    @Test
    void getOrCreateChat_SameUser() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(1L)
                .build();

        Chat selfChat = Chat.builder()
                .id(3L)
                .userA(testUserA)
                .userB(testUserA)
                .createdAt(Instant.now())
                .build();

        when(chatService.getOrCreateChat(1L, 1L)).thenReturn(selfChat);

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.userAId").value(1L))
                .andExpect(jsonPath("$.userBId").value(1L));

        verify(chatService).getOrCreateChat(1L, 1L);
    }

    @Test
    void getOrCreateChat_ReverseOrder() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(2L)
                .userBId(1L)
                .build();

        Chat reverseChat = Chat.builder()
                .id(1L)
                .userA(testUserB)
                .userB(testUserA)
                .createdAt(Instant.now())
                .build();

        when(chatService.getOrCreateChat(2L, 1L)).thenReturn(reverseChat);

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userAId").value(2L))
                .andExpect(jsonPath("$.userBId").value(1L));

        verify(chatService).getOrCreateChat(2L, 1L);
    }

    @Test
    void getOrCreateChat_ServiceReturnsNull() throws Exception {
        // Given
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(2L)
                .build();

        when(chatService.getOrCreateChat(1L, 2L)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status < 400 || status >= 600) {
                        throw new AssertionError("Expected 4xx or 5xx status but was: " + status);
                    }
                });

        verify(chatService).getOrCreateChat(1L, 2L);
    }}