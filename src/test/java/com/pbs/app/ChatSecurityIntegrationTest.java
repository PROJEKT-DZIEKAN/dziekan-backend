package com.pbs.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.app.dto.ChatDTO;
import com.pbs.app.models.Chat;
import com.pbs.app.models.User;
import com.pbs.app.repositories.ChatRepository;
import com.pbs.app.repositories.MessageRepository;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.services.JWTService;
import com.pbs.app.enums.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
//@TestPropertySource(properties = {
//        "spring.datasource.url=jdbc:h2:mem:testdb",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//        "allowed.origins=http://localhost:3000,http://localhost:8080"
//})
//@Transactional
public class ChatSecurityIntegrationTest {

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

    @MockBean
    private JWTService jwtService;

    private MockMvc mockMvc;

    private User testUserA;
    private User testUserB;
    private Chat testChat;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

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
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenNotAuthenticatedForCreateChat() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserB.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenInvalidJwtToken() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "Bearer invalid-token")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenMalformedJwtToken() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "Bearer malformed.jwt.token")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenExpiredJwtToken() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "Bearer expired.jwt.token")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenInvalidAuthorizationHeaderFormat() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "InvalidFormat token")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenEmptyAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenNullBearerToken() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("Authorization", "Bearer ")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForPostRequestWithoutAuth() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserB.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForPostRequestWithInvalidAuth() throws Exception {
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(testUserA.getId())
                .userBId(testUserB.getId())
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowCorsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/chats")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void shouldRejectCorsRequestFromUnallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/chats")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization")
                        .header("Origin", "http://malicious-site.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowOptionsRequestWithoutAuth() throws Exception {
        mockMvc.perform(options("/api/chats")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401ForGetRequestWithoutParamsAndAuth() throws Exception {
        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForAllChatEndpointsWithoutAuth() throws Exception {
        // Test GET /api/chats
        mockMvc.perform(get("/api/chats")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());

        // Test POST /api/chats/get-or-create  
        ChatDTO requestDto = ChatDTO.builder()
                .userAId(1L)
                .userBId(2L)
                .build();

        mockMvc.perform(post("/api/chats/get-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}