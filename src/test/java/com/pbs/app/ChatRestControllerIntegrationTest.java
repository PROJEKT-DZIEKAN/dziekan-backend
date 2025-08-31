package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.dto.ChatDTO;
import com.pbs.app.models.Chat;
import com.pbs.app.models.Message;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.MessageRepository;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.enums.RegistrationStatus;
import com.pbs.app.services.JWTService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
//@TestPropertySource(properties = {
//        "spring.datasource.url=jdbc:h2:mem:testdb",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//        "allowed.origins=http://localhost:3000,http://localhost:8080"
//})
@Transactional
public class ChatRestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private JWTService jwtService;

    private MockMvc mockMvc;

    private User testUserA;
    private User testUserB;
    private User testUserC;
    private Chat testChat;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();


        // Wyczyszczenie bazy danych w odpowiedniej kolejności
        messageRepository.deleteAll();
        chatRepository.deleteAll();

        // Dodaj czyszczenie tabeli notification_users
        entityManager.createNativeQuery("DELETE FROM notification_users").executeUpdate();
        entityManager.flush();

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

        testUserC = User.builder()
                .firstName("Piotr")
                .surname("Zieliński")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .groups(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        testUserC = userRepository.save(testUserC);

        testChat = Chat.builder()
                .userA(testUserA)
                .userB(testUserB)
                .createdAt(Instant.now())
                .build();
        testChat = chatRepository.save(testChat);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldListChatsByUserId() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .param("userId", testUserA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testChat.getId()))
                .andExpect(jsonPath("$[0].userAId").value(testUserA.getId()))
                .andExpect(jsonPath("$[0].userBId").value(testUserB.getId()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldListMultipleChatsByUserId() throws Exception {
        // Dodanie drugiego czatu
        Chat secondChat = Chat.builder()
                .userA(testUserA)
                .userB(testUserC)
                .createdAt(Instant.now())
                .build();
        chatRepository.save(secondChat);

        mockMvc.perform(get("/api/chats")
                        .param("userId", testUserA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].userAId", containsInAnyOrder(
                        testUserA.getId().intValue(), testUserA.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnEmptyListWhenUserHasNoChats() throws Exception {
        // Tworzenie użytkownika bez czatów
        User userWithoutChats = User.builder()
                .firstName("Maria")
                .surname("Kowalczyk")
                .registrationStatus(RegistrationStatus.REGISTERED)
                .groups(new HashSet<>())
                .roles(new HashSet<>())
                .build();
        userWithoutChats = userRepository.save(userWithoutChats);

        mockMvc.perform(get("/api/chats")
                        .param("userId", userWithoutChats.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnBadRequestWhenUserIdParameterMissing() throws Exception {
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnBadRequestWhenUserIdParameterInvalid() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .param("userId", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldCreateNewChatWhenNotExists() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserC.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userAId").value(testUserA.getId()))
                .andExpect(jsonPath("$.userBId").value(testUserC.getId()));

        // Sprawdzenie czy czat został utworzony w bazie
        List<Chat> chats = chatRepository.findByUserA_IdOrUserB_Id(testUserA.getId(), testUserA.getId());
        assertEquals(2, chats.size()); // Już jeden istniał + nowy
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnExistingChatWhenExists() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserB.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testChat.getId()))
                .andExpect(jsonPath("$.userAId").value(testUserA.getId()))
                .andExpect(jsonPath("$.userBId").value(testUserB.getId()));

        // Sprawdzenie czy nie utworzono nowego czatu
        List<Chat> chats = chatRepository.findByUserA_IdOrUserB_Id(testUserA.getId(), testUserA.getId());
        assertEquals(1, chats.size());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnExistingChatWhenUsersReversed() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserB.getId())
                .userBId(testUserA.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testChat.getId()))
                .andExpect(jsonPath("$.userAId").value(testUserA.getId()))
                .andExpect(jsonPath("$.userBId").value(testUserB.getId()));

        // Sprawdzenie czy nie utworzono nowego czatu
        List<Chat> chats = chatRepository.findByUserA_IdOrUserB_Id(testUserA.getId(), testUserA.getId());
        assertEquals(1, chats.size());
    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    void shouldReturnBadRequestWhenUserANotFound() throws Exception {
//        ChatDTO requestDto = ChatDTO.builder()
//                .userAId(999L)
//                .userBId(testUserB.getId())
//                .build();
//
//        mockMvc.perform(post("/api/chats/get-or-create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isBadRequest());
//    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    void shouldReturnBadRequestWhenUserBNotFound() throws Exception {
//        ChatDTO requestDto = ChatDTO.builder()
//                .userAId(testUserA.getId())
//                .userBId(999L)
//                .build();
//
//        mockMvc.perform(post("/api/chats/get-or-create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldHandleInvalidJsonInGetOrCreate() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldHandleEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
//
//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    void shouldHandleNullUserIds() throws Exception {
//        ChatDTO requestDto = ChatDTO.builder()
//                .userAId(null)
//                .userBId(null)
//                .build();
//
//        mockMvc.perform(post("/api/chats/get-or-create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldCreateChatWithSameUser() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserA.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userAId").value(testUserA.getId()))
                .andExpect(jsonPath("$.userBId").value(testUserA.getId()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldListChatsForUserBAsWell() throws Exception {
        // Test czy userB również widzi czat
        mockMvc.perform(get("/api/chats")
                        .param("userId", testUserB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testChat.getId()));
    }}

//
//    @Test
//    void shouldMaintainChatConsistencyWithMessages() throws Exception {
//        // Dodanie wiadomości do czatu
//        Message message = Message.builder()
//                .chat(testChat)
//                .sender(testUserA)
//                .content("Test message")
//                .sentAt(Instant.now())
//                .build();
//        messageRepository.save(message);
//
//        // Synchronizacja kontekstu persistence
//        entityManager.flush();
//        entityManager.clear();
//
//        // Sprawdzenie czy czat nadal istnieje z wiadomością
//        mockMvc.perform(get("/api/chats")
//                        .param("userId", testUserA.getId().toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].id").value(testChat.getId()));
//
//        // Sprawdzenie czy wiadomość jest powiązana z czatem
//        Optional<Chat> chatOpt = chatRepository.findById(testChat.getId());
//        assertTrue(chatOpt.isPresent());
//        assertEquals(1, chatOpt.get().getMessages().size());
//    }}
