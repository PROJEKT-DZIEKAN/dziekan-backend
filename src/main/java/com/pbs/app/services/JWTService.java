package com.pbs.app.services;

import com.pbs.app.security.JWTConfig;
import com.pbs.app.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JWTService {

    private final JWTConfig jwtConfig;

    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60; //minuta
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 4; //4 dni

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("firstName", user.getFirstName())
                .claim("surname", user.getSurname())
                .claim("status", user.getRegistrationStatus().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, String expectedUserId) {
        try {
            String id = extractUserId(token);
            return id.equals(expectedUserId) && !isExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
