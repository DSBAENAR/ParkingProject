package com.parking.core.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.AuthRequest;
import com.parking.core.auth.model.BlackListToken;
import com.parking.core.auth.services.AuthService;
import com.parking.core.auth.services.JWTService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("api/v1/parking/auth")
public class AuthHandler {
    private final AuthService authService;
    private final BlackListToken blackList;
    private final JWTService Jwts;
    
    
    public AuthHandler(AuthService authService, BlackListToken blacklist, JWTService jwts) {
        this.blackList = blacklist;
        this.authService = authService;
        this.Jwts = jwts;
    }


    /**
     * Handles the user sign-up process.
     *
     * @param request the authentication request containing user credentials and details
     * @return a ResponseEntity containing a success message and additional response data
     *         if the sign-up is successful, or an error message if an exception occurs
     * @throws ResponseStatusException if there is an issue during the sign-up process,
     *         such as invalid input or a conflict
     */
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody AuthRequest request) {
        try {
            Map<String,Object> response = authService.signUp(request);
            response.put("message", "user created succesfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                            .status(e.getStatusCode())
                            .body(Collections.singletonMap("message", e.getReason()));
        }
        
    }

    @PostMapping("/signUp/bulk")
    public ResponseEntity<?> signUpBulk(@RequestBody List<AuthRequest> requests) {
        List<Map<String,Object>> responses = new ArrayList<>();

        for (AuthRequest req : requests) {
            try {
                Map<String,Object> result = authService.signUp(req);
                result.put("message", "user created successfully");
                responses.add(result);
            } catch (ResponseStatusException e){
                responses.add(Map.of(
                    "status", e.getStatusCode(),
                    "username", req.getUsername(),
                    "message", e.getReason()
                ));
            }
            
            catch (Exception e) {
                responses.add(Map.of(
                    "status", "error",
                    "username", req.getUsername(),
                    "message", "Unexpected error: " + e.getMessage()
                ));
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "processed", requests.size(),
                "results", responses
        ));
    }
    

    /**
     * Handles the login request for a user.
     *
     * @param request the authentication request containing user credentials.
     *                It must be an {@link AuthRequest} object.
     * @return a {@link ResponseEntity} containing a map with the login response.
     *         If the login is successful, the response includes a success message
     *         and additional data. If an error occurs, it returns an appropriate
     *         HTTP status code and an error message.
     * @throws ResponseStatusException if there is an issue during the login process,
     *                                 such as invalid credentials or server errors.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
       try {
        Map<String,Object> response = authService.login(request);
        response.put("message", "User logged-in correctly");
        return ResponseEntity.ok(response);
       } catch (ResponseStatusException e) {
        return ResponseEntity
                        .status(e.getStatusCode())
                        .body(Collections.singletonMap("message", e.getReason()));
       }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String header) {
        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
           long expirationMs = Jwts.getExpiration(token);
           blackList.add(token, expirationMs);
        }
        return ResponseEntity.ok(Map.of("message","Logout successfully"));
    }
    
    
    
    
}
