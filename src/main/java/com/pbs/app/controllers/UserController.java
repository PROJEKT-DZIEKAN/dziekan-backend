package com.pbs.app.controllers;

import com.pbs.app.models.User;
import com.pbs.app.repositories.UserRepository;
import com.pbs.app.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JWTService jwtService;

    // ------------------------ QR LOGIN (po userId) ------------------------

//    @PostMapping("/auth/qr-login") //endpoint ktory umozliwi po zeskanowaniu kodu qr zalogowac sie uzytkownikowi i otrzyma access token oraz refresh
//    public ResponseEntity<?> qrLogin(@RequestBody QrLoginRequest request) {
//        Optional<User> userOpt = userRepository.findById(request.userId());
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.status(401).body("Invalid user ID");
//        }
//        User user = userOpt.get();
//        String access  = jwtService.generateAccessToken(user);
//        String refresh = jwtService.generateRefreshToken(user);
//        return ResponseEntity.ok(new TokenResponse(access, refresh));
//    }

    @GetMapping("/users/me") //wrazie potrzeby endpoint aby odpytac baze o informacje o aktualnego usera
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token  = authHeader.replace("Bearer ", "");
        String userId = jwtService.extractUserId(token);
        Optional<User> opt = userRepository.findById(Long.parseLong(userId));
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("User not found");
        }
    }

    // ------------------------ CRUD dla User ------------------------

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("User not found");
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody User updatedUser) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("User not found");
        }
        User user = opt.get();
        user.setFirstName(updatedUser.getFirstName());
        user.setSurname(updatedUser.getSurname());
        user.setRegistrationStatus(updatedUser.getRegistrationStatus());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    User user = User.builder()
            .firstName(request.firstName())
            .surname(request.surname())
            .build();
    userRepository.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body("User registered");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> u.getFirstName().equalsIgnoreCase(request.firstName()) &&
                             u.getSurname().equalsIgnoreCase(request.surname()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        User user = userOpt.get();
        String access  = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }




    // ------------------------ DTOs ------------------------
    private record RegisterRequest(String firstName, String surname) {}
    private record LoginRequest(String firstName, String surname) {}
    private record TokenResponse(String accessToken, String refreshToken) {}
    private record QrLoginRequest(Long userId) {}
}
