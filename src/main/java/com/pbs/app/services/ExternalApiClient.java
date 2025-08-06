package com.pbs.app.services;

import com.pbs.app.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalApiClient {

    @Value("${external.api.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final UserService userService;
    private String token;

    public Optional<String> login(String username, String password) {
        try {
            String url = baseUrl + "/api/authenticate";
            System.out.println("Logging in to URL: " + url);

            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());

            if (response.getBody() != null) {
                this.token = (String) response.getBody().get("jwt");

                if (this.token != null && !this.token.isEmpty()) {
                    System.out.println("JWT token saved successfully!");
                    return Optional.of(this.token);
                }
            }

        } catch (Exception e) {
            System.out.println("Login error: " + e.getClass().getSimpleName());
            System.out.println("Details: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Map getUserDetails() {
        if (token == null) return null;

        try {
            String url = baseUrl + "/api/user/details";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Error in getUserDetails: " + e.getMessage());
        }
        return null;
    }

    public List getAllUsers() {
        if (token == null) return null;

        try {
            String url = baseUrl + "/api/user/all";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
            return response.getBody();
        } catch (Exception e) {
            System.out.println("Error in getAllUsers: " + e.getMessage());
        }
        return null;
    }

    public void loginAsManagerAndFetchAll() {
        System.out.println("=== LOGGING IN AS MANAGER ===");

        Optional<String> loginResult = login("aplikacja", "Zj@zd2o26");

        if (loginResult.isEmpty()) {
            System.out.println("Error while logging in as manager.");
            return;
        }

        System.out.println("Logged in as manager with token: " + loginResult.get().substring(0, 20) + "...");

        System.out.println("\n=== FETCHING AND SAVING USERS ===");
        List allUsers = getAllUsers();

        if (allUsers != null) {
            System.out.println("Number of users from API: " + allUsers.size());
            int savedCount = 0;
            int skippedCount = 0;

            for (Object user : allUsers) {
                try {
                    Map userMap = (Map) user;

                    String firstName = (String) userMap.get("firstName");
                    String lastName = (String) userMap.get("lastName");
                    String position = (String) userMap.get("position");
                    String university = (String) userMap.get("university");
                    String department = (String) userMap.get("department");
                    String email = (String) userMap.get("email");
                    String userId = (String) userMap.get("userID");

                    System.out.println("Processing: " + firstName + " " + lastName);

                    // Check if user already exists
                    if (userService.existsByUserID(userId)) {
                        System.out.println("User already exists, skipping: " + userId + " (" + firstName + " " + lastName + ")");
                        skippedCount++;
                        continue;
                    }

                    // Create and save user
                    User newUser = User.builder()
                            .firstName(firstName)
                            .surname(lastName)
                            .position(position)
                            .university(university)
                            .department(department)
                            .email(email)
                            .userID(userId)
                            .build();
                    System.out.println("Creating user: " + newUser.getFirstName() + " " + newUser.getSurname() + " (Position: " + newUser.getPosition() + ") (University: " + newUser.getUniversity() + ") (Department: " + newUser.getDepartment() + ") (Email: " + newUser.getEmail() + ")") ;
                    User savedUser = userService.createUser(newUser);
                    System.out.println("Saved: " + savedUser.getFirstName() + " " + savedUser.getSurname() + " (ID: " + savedUser.getId() + ")");
                    savedCount++;

                } catch (Exception e) {
                    System.out.println("Error saving user: " + e.getMessage());
                }
            }

            System.out.println("\nSUMMARY:");
            System.out.println("New users saved: " + savedCount);
            System.out.println("Existing users skipped: " + skippedCount);
            System.out.println("Total processed: " + allUsers.size());

        } else {
            System.out.println("Failed to fetch users from API");
        }
    }
}