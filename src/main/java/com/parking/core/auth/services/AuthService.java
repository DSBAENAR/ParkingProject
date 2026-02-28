package com.parking.core.auth.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.AuthRequest;
import com.parking.core.auth.model.Response.AuthResponse;
import com.parking.core.enums.Roles;
import com.parking.core.model.User;
import com.parking.core.repository.UserRepository;

/**
 * Service layer for authentication operations (sign-up and login).
 * <p>
 * Handles user registration with BCrypt password encoding and JWT token generation,
 * as well as login authentication using Spring Security's {@link AuthenticationManager}.
 * </p>
 *
 * @see JWTService
 * @see AuthRequest
 * @see AuthResponse
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwt;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, JWTService jwt,
            PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
        this.encoder = encoder;
    }

    /**
     * Registers a new user in the system.
     * <p>
     * Checks for duplicate username/email, encodes the password with BCrypt,
     * assigns the {@code USER} role, and generates a JWT token.
     * </p>
     *
     * @param request the registration data containing name, username, email, and password
     * @return a map with the {@link AuthResponse} and JWT token
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if the username or email already exists
     */
    @Transactional
    public Map<String, Object> signUp(AuthRequest request) {
        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The username or email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Roles.USER);
        user.setEmail(request.getEmail());

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("user", new AuthResponse(user.getName(), user.getEmail(), user.getUsername()));
        response.put("token", jwt.generateToken(user));
        return response;
    }

    /**
     * Authenticates a user and generates a JWT token.
     * <p>
     * Accepts either username or email as the identifier. Falls back to email
     * if username is blank or null.
     * </p>
     *
     * @param request the login data containing username/email and password
     * @return a map with the {@link AuthResponse} and JWT token
     * @throws ResponseStatusException with {@code 401 UNAUTHORIZED} if credentials are missing, invalid, or user not found
     */
    public Map<String, Object> login(AuthRequest request) {
        String identifier = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername() : request.getEmail();

        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credentials are missing");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword()));

            User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            log.info("User logged in: {}", user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("user", new AuthResponse(user.getName(), user.getEmail(), user.getUsername()));
            response.put("token", jwt.generateToken(user));
            return response;

        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username, email or password are incorrect");
        }
    }
}
