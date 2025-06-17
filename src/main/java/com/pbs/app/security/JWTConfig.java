package com.pbs.app.security;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JWTConfig {

    @Value("${jwt.secret}") //bierze klucz z pliku application.properties
    private String secret;

    @Getter
    private SecretKey secretKey;

    @PostConstruct // sprawda czy klucz jwt ma poprawna ilosc znakow
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters long.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
