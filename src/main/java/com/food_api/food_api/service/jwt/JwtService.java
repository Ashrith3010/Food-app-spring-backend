package com.food_api.food_api.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import com.food_api.food_api.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    // Secret key for signing JWT tokens
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Generate JWT token from User entity
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userType", user.getType());
        claims.put("userId", user.getId().toString()); // Convert Long to String
        claims.put("name", user.getUsername());
        claims.put("phoneNumber", user.getPhone());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours expiration
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract the user ID from the JWT token
    public String getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        Object userId = claims.get("userId");

        // Handle both Integer and String types for userId
        if (userId instanceof Integer) {
            return String.valueOf(userId);
        } else if (userId instanceof String) {
            return (String) userId;
        }

        return null;
    }

    // Extract email from JWT token
    public String getEmailFromToken(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    // Extract phone number from JWT token
    public String getPhoneNumberFromToken(String token) {
        return extractClaim(token, claims -> claims.get("phoneNumber", String.class));
    }

    // Extract claims from the token
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract a specific claim from the token using a claims resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract username from the token (subject of the token)
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // If parsing is successful, the token is valid
            return true;
        } catch (Exception e) {
            return false; // Invalid token if exception occurs
        }
    }

    // Extract name from the token
    public String getNameFromToken(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }
}
