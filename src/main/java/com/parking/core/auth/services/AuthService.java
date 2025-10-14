package com.parking.core.auth.services;


import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.AuthRequest;
import com.parking.core.auth.model.Response.AuthResponse;
import com.parking.core.enums.Roles;
import com.parking.core.model.User;
import com.parking.core.repository.UserRepository;


@Service
public class AuthService {

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
     *
     * @param request The authentication request containing the user's details such as
     *                name, username, email, and password.
     * @return A map containing the registered user object and a generated JWT token.
     * @throws ResponseStatusException If the username or email is already registered, or
     *                                 if the email format is invalid.
     */
    public Map<String,Object> signUp(AuthRequest request){

        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The username or email is already registered");
        }

        else if (!request.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.+[A-Za-z]{2,}$")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The email is wrong");
        }
        
        
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Roles.USER);
        user.setEmail(request.getEmail());

        userRepository.save(user);

        Map<String,Object> response = new HashMap<>();
        AuthResponse userResponse = new AuthResponse(user.getName(), user.getEmail(), user.getUsername());
        response.put("user", userResponse);
        response.put("token", jwt.generateToken(user));
        return response;
    }

    /**
     * Authenticates a user based on the provided login credentials and generates a response
     * containing user details and a JWT token.
     *
     * @param request The authentication request containing the username or email and password.
     * @return A map containing the authenticated user's details and a generated JWT token.
     * @throws ResponseStatusException If the email format is invalid, the credentials are missing,
     *                                 or authentication fails due to invalid credentials.
     */
    public Map<String,Object> login(AuthRequest request) {
        String identifier = (request.getUsername() != null && !request.getUsername().isBlank())
                            ? request.getUsername():request.getEmail();
        
        
        if (identifier == null || identifier.isBlank()) 
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "The credentials does not exists or are missing");

        try {
            @SuppressWarnings("unused")
            Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        
        User user = userRepository.findByUsernameOrEmail(identifier,identifier).get();
        AuthResponse userResponse = new AuthResponse(user.getName(), user.getEmail(), user.getUsername());
        Map<String,Object> response = new HashMap<>();

        response.put("user",userResponse );
        response.put("token", jwt.generateToken(user));

        return response;
        
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        catch (BadCredentialsException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username, email or password are incorrect");
        }
    }

    
}
