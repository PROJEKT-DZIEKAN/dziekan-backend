import com.pbs.app.controllers.UserController;
import com.pbs.app.models.User;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.services.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setSurname("Doe");
        testUser.setRegistrationStatus("ACTIVE");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setFirstName("Jane");
        testUser2.setSurname("Smith");
        testUser2.setRegistrationStatus("PENDING");
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.surname").value(testUser.getSurname()));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testUser2));

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUser.getId()))
                .andExpect(jsonPath("$[1].id").value(testUser2.getId()))
                .andExpect(jsonPath("$.length()").value(2));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUser_shouldReturnUser_whenUserExists() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()));

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser_whenUserExists() throws Exception {
        User updatedDetails = new User();
        updatedDetails.setFirstName("Johnny");
        updatedDetails.setSurname("Depp");
        updatedDetails.setRegistrationStatus("BLOCKED");

        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setFirstName("Johnny");
        updatedUser.setSurname("Depp");
        updatedUser.setRegistrationStatus("BLOCKED");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.firstName").value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.surname").value(updatedUser.getSurname()))
                .andExpect(jsonPath("$.registrationStatus").value(updatedUser.getRegistrationStatus()));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        User updatedDetails = new User();
        updatedDetails.setFirstName("Johnny");
        updatedDetails.setSurname("Depp");
        updatedDetails.setRegistrationStatus("BLOCKED");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldReturnOk_whenUserExists() throws Exception {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/users/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userRepository, times(1)).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenTokenIsValid() throws Exception {
        String token = "valid.jwt.token";
        String userId = "1";

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()));

        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getCurrentUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String token = "valid.jwt.token";
        String userId = "99";

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(jwtService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void register_shouldReturnCreatedStatus() throws Exception {
        record RegisterRequest(String firstName, String surname) {}
        RegisterRequest registerRequest = new RegisterRequest("New", "User");

        when(userRepository.save(any(User.class))).thenReturn(any(User.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        record LoginRequest(String firstName, String surname) {}
        record TokenResponse(String accessToken, String refreshToken) {}
        LoginRequest loginRequest = new LoginRequest("John", "Doe");
        TokenResponse tokenResponse = new TokenResponse("access_token", "refresh_token");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testUser2));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh_token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(tokenResponse.accessToken()))
                .andExpect(jsonPath("$.refreshToken").value(tokenResponse.refreshToken()));

        verify(userRepository, times(1)).findAll();
        verify(jwtService, times(1)).generateAccessToken(testUser);
        verify(jwtService, times(1)).generateRefreshToken(testUser);
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        record LoginRequest(String firstName, String surname) {}
        LoginRequest loginRequest = new LoginRequest("Invalid", "User");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testUser2));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(userRepository, times(1)).findAll();
        verify(jwtService, never()).generateAccessToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken(any(User.class));
    }
}