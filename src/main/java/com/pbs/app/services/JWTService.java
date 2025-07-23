package com.pbs.app.services;

import com.pbs.app.models.User;
import com.pbs.app.security.JWTConfig;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.*;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JWTService {

    private final JWTConfig jwtConfig;

    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 30;           // 30 minutes
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 4; // 4 days

    private JwtParser parser;
    private SecretKey signingKey;

    public JWTService(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() {
        this.signingKey = jwtConfig.getSecretKey();
        this.parser = Jwts.parserBuilder()
                          .setSigningKey(signingKey)
                          .setAllowedClockSkewSeconds(30)   // allow 30s drift
                          .build();
    }

    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();

        List<String> roleNames = user.getRoles().stream()
                                 .map(Role::getRoleName)
                                 .collect(Collectors.toList());
        return Jwts.builder()
                   .setSubject(user.getId().toString())
                   .claim("firstName", user.getFirstName())
                   .claim("surname",   user.getSurname())
                   .claim("status",    user.getRegistrationStatus().name())
                   .claim("role",      roleNames)
                   .setIssuedAt(new Date(now))
                   .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRATION))
                   .signWith(signingKey, SignatureAlgorithm.HS256)
                   .compact();
    }

    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                   .setSubject(user.getId().toString())
                   .setIssuedAt(new Date(now))
                   .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRATION))
                   .signWith(signingKey, SignatureAlgorithm.HS256)
                   .compact();
    }

    public String extractUserId(String token) throws JwtException {
        Jws<Claims> claims = parser.parseClaimsJws(token);
        return claims.getBody().getSubject();
    }

    public boolean isTokenValid(String token, String expectedUserId) {
        try {
            String sub = extractUserId(token);
            return sub.equals(expectedUserId);
        } catch (ExpiredJwtException eje) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }
}
