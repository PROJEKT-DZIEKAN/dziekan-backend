import com.pbs.app.controllers.UserController;
import com.pbs.app.models.User;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.services.JWTService; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.mock.mockito.MockBean; 
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockBean 
    private JWTService jwtService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";
        userRepository.deleteAll(); 
    
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mockedAccessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mockedRefreshToken");
        when(jwtService.extractUserId("mockedAccessToken")).thenReturn("1"); 
    }

    @Test
    void userIntegrationScenario() {
        User newUser = new User();
        newUser.setFirstName("Integration");
        newUser.setSurname("Test");
        newUser.setRegistrationStatus("ACTIVE");

        ResponseEntity<User> createUserResponse = restTemplate.postForEntity(baseUrl, newUser, User.class);
        assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(createUserResponse.getBody()).getId()).isNotNull();
        assertThat(createUserResponse.getBody().getFirstName()).isEqualTo("Integration");
        Long userId = createUserResponse.getBody().getId();

       
        ResponseEntity<User> getUserResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(getUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(getUserResponse.getBody()).getFirstName()).isEqualTo("Integration");

        
        ResponseEntity<List> getAllUsersResponse = restTemplate.getForEntity(baseUrl, List.class);
        assertThat(getAllUsersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(getAllUsersResponse.getBody()).size()).isEqualTo(1);

        
        User updatedUser = new User();
        updatedUser.setFirstName("Updated");
        updatedUser.setSurname("User");
        updatedUser.setRegistrationStatus("BLOCKED");

        restTemplate.put(baseUrl + "/" + userId, updatedUser);

        ResponseEntity<User> getUpdatedUserResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(getUpdatedUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(getUpdatedUserResponse.getBody()).getFirstName()).isEqualTo("Updated");
        assertThat(getUpdatedUserResponse.getBody().getSurname()).isEqualTo("User");
        assertThat(getUpdatedUserResponse.getBody().getRegistrationStatus()).isEqualTo("BLOCKED");


       
        restTemplate.delete(baseUrl + "/" + userId);
        ResponseEntity<String> deleteResponse = restTemplate.getForEntity(baseUrl + "/" + userId, String.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND); 

       
        ResponseEntity<User> getAfterDeleteResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void registerAndLoginIntegration() {
        
        record RegisterRequest(String firstName, String surname) {}
        RegisterRequest registerRequest = new RegisterRequest("New", "User");

        
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isEqualTo("User registered");

       
        record LoginRequest(String firstName, String surname) {}
        record TokenResponse(String accessToken, String refreshToken) {}
        LoginRequest loginRequest = new LoginRequest("New", "User");

        ResponseEntity<TokenResponse> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login", loginRequest, TokenResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(loginResponse.getBody()).accessToken()).isEqualTo("mockedAccessToken");
        assertThat(loginResponse.getBody().refreshToken()).isEqualTo("mockedRefreshToken");

        
        Optional<User> foundUser = userRepository.findAll().stream()
                .filter(u -> u.getFirstName().equalsIgnoreCase("New") && u.getSurname().equalsIgnoreCase("User"))
                .findFirst();
        assertThat(foundUser).isPresent();

        
        when(jwtService.extractUserId("mockedAccessToken")).thenReturn(String.valueOf(foundUser.get().getId()));

        ResponseEntity<User> currentUserResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/me",
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createHeaders("mockedAccessToken")),
                User.class
        );
        assertThat(currentUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(currentUserResponse.getBody()).getFirstName()).isEqualTo("New");
    }

    private org.springframework.http.HttpHeaders createHeaders(String token) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
