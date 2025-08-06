package com.pbs.app.controllers;

import com.pbs.app.services.ExternalApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/external-api")
@RequiredArgsConstructor
public class ExternalApiController {

    private final ExternalApiClient apiClient;

    @PostMapping("/login")
    public String authentication(@RequestParam String username, @RequestParam String password) {
        Optional<String> token = apiClient.login(username, password);
        if (token.isPresent()) {
            return token.get();
        }
        return "Błąd logowania!";
    }

    @GetMapping("/user")
    public Object getUser() {
        return apiClient.getUserDetails();
    }

    // Zmień na GET żeby ominąć CSRF
    @GetMapping("/fetch-all")  // Zmienione z POST na GET
    public String fetchAllData() {
        apiClient.loginAsManagerAndFetchAll();
        return "Sprawdź konsole - wszystkie dane zostały pobrane!";
    }

    // Dodaj endpoint do pobierania CSRF tokenu
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
