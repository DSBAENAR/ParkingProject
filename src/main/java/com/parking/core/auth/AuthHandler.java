package com.parking.core.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.AuthRequest;
import com.parking.core.auth.model.BlackListToken;
import com.parking.core.auth.services.AuthService;
import com.parking.core.auth.services.JWTService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("api/v1/parking/auth")
public class AuthHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);

    private final AuthService authService;
    private final BlackListToken blackList;
    private final JWTService jwtService;

    public AuthHandler(AuthService authService, BlackListToken blacklist, JWTService jwtService) {
        this.blackList = blacklist;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signUp")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody AuthRequest request) {
        Map<String, Object> response = authService.signUp(request);
        response.put("message", "User created successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signUp/bulk")
    public ResponseEntity<Map<String, Object>> signUpBulk(@RequestBody List<AuthRequest> requests) {
        List<Map<String, Object>> responses = new ArrayList<>();

        for (AuthRequest req : requests) {
            try {
                Map<String, Object> result = authService.signUp(req);
                result.put("message", "User created successfully");
                responses.add(result);
            } catch (ResponseStatusException e) {
                log.warn("Bulk signUp failed for user {}: {}", req.getUsername(), e.getReason());
                responses.add(Map.of(
                        "status", e.getStatusCode().value(),
                        "username", req.getUsername(),
                        "message", e.getReason()));
            } catch (Exception e) {
                log.error("Unexpected error in bulk signUp for user {}", req.getUsername(), e);
                responses.add(Map.of(
                        "status", 500,
                        "username", req.getUsername(),
                        "message", "Unexpected error: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(Map.of(
                "processed", requests.size(),
                "results", responses));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest request) {
        Map<String, Object> response = authService.login(request);
        response.put("message", "User logged-in correctly");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            long expirationMs = jwtService.getExpiration(token);
            blackList.add(token, expirationMs);
        }
        return ResponseEntity.ok(Map.of("message", "Logout successfully"));
    }
}
