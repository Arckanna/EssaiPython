package com.gamesUP.gamesUP.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;            // <--- ici
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenService {
    private final SecretKey key;          // <--- ici
    private final String issuer;
    private final long expiryMinutes;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.issuer}") String issuer,
                           @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expiryMinutes = expiryMinutes;
    }

    public String generate(String subjectEmail, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectEmail)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiryMinutes * 60)))
                .claim("role", role)
                .signWith(key, Jwts.SIG.HS256)    // OK en 0.12.x avec SecretKey
                .compact();
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt);
    }
}
