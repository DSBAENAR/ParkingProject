package com.parking.core.auth.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.parking.core.enums.Roles;

import io.jsonwebtoken.ExpiredJwtException;

class JWTServiceTest {

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        // Same key as in test application.properties
        String secretBase64 = "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2Vz";
        jwtService = new JWTService(secretBase64, 3600000L);
    }

    private com.parking.core.model.User buildUser() {
        com.parking.core.model.User user = new com.parking.core.model.User();
        user.setName("John");
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setPassword("encoded");
        user.setRole(Roles.USER);
        return user;
    }

    @Test
    @DisplayName("generateToken - produces non-null token")
    void generateToken_success() {
        String token = jwtService.generateToken(buildUser());
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("extractUsername - returns correct username")
    void extractUsername_success() {
        String token = jwtService.generateToken(buildUser());
        String username = jwtService.extractUsername(token);
        assertEquals("john", username);
    }

    @Test
    @DisplayName("isValidToken - valid token returns true")
    void isValidToken_true() {
        String token = jwtService.generateToken(buildUser());
        UserDetails userDetails = User.builder()
                .username("john")
                .password("encoded")
                .roles("USER")
                .build();

        assertTrue(jwtService.isValidToken(token, userDetails));
    }

    @Test
    @DisplayName("isValidToken - wrong username returns false")
    void isValidToken_wrongUsername() {
        String token = jwtService.generateToken(buildUser());
        UserDetails userDetails = User.builder()
                .username("other")
                .password("encoded")
                .roles("USER")
                .build();

        assertFalse(jwtService.isValidToken(token, userDetails));
    }

    @Test
    @DisplayName("isValidToken - expired token throws ExpiredJwtException")
    void isValidToken_expired() {
        // Create service with 0ms expiration (token immediately expires)
        JWTService shortLived = new JWTService(
                "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2Vz", 0L);
        String token = shortLived.generateToken(buildUser());

        UserDetails userDetails = User.builder()
                .username("john")
                .password("encoded")
                .roles("USER")
                .build();

        // JJWT throws ExpiredJwtException when parsing an expired token
        assertThrows(ExpiredJwtException.class,
                () -> shortLived.isValidToken(token, userDetails));
    }

    @Test
    @DisplayName("getExpiration - returns positive value for fresh token")
    void getExpiration_positive() {
        String token = jwtService.generateToken(buildUser());
        long exp = jwtService.getExpiration(token);
        assertTrue(exp > 0);
    }
}
