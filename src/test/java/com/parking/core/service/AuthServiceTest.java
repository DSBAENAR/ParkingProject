package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.AuthRequest;
import com.parking.core.auth.model.Response.AuthResponse;
import com.parking.core.auth.services.AuthService;
import com.parking.core.auth.services.JWTService;
import com.parking.core.enums.Roles;
import com.parking.core.model.User;
import com.parking.core.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwt;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    private AuthRequest signUpRequest;
    private AuthRequest loginRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new AuthRequest();
        signUpRequest.setName("John Doe");
        signUpRequest.setUsername("johndoe");
        signUpRequest.setEmail("john@example.com");
        signUpRequest.setPassword("password123");

        loginRequest = new AuthRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("signUp")
    class SignUpTests {

        @Test
        @DisplayName("should register a new user successfully")
        void shouldRegisterNewUser() {
            when(userRepository.findByUsernameOrEmail("johndoe", "john@example.com"))
                    .thenReturn(Optional.empty());
            when(encoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwt.generateToken(any(User.class))).thenReturn("jwt-token-123");

            Map<String, Object> result = authService.signUp(signUpRequest);

            assertNotNull(result);
            assertNotNull(result.get("token"));
            assertEquals("jwt-token-123", result.get("token"));

            AuthResponse authResponse = (AuthResponse) result.get("user");
            assertEquals("John Doe", authResponse.name());
            assertEquals("john@example.com", authResponse.email());
            assertEquals("johndoe", authResponse.username());

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw 400 when username or email already exists")
        void shouldThrow400WhenDuplicate() {
            User existing = new User("Existing", "johndoe", Roles.USER, "john@example.com", null);
            when(userRepository.findByUsernameOrEmail("johndoe", "john@example.com"))
                    .thenReturn(Optional.of(existing));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.signUp(signUpRequest));

            assertEquals(400, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("already registered"));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePassword() {
            when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(encoder.encode("password123")).thenReturn("$2a$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwt.generateToken(any(User.class))).thenReturn("token");

            authService.signUp(signUpRequest);

            verify(encoder).encode("password123");
        }

        @Test
        @DisplayName("should assign USER role to new users")
        void shouldAssignUserRole() {
            when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(encoder.encode(anyString())).thenReturn("encoded");
            when(jwt.generateToken(any(User.class))).thenReturn("token");
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                assertEquals(Roles.USER, u.getRole());
                return u;
            });

            authService.signUp(signUpRequest);

            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should login successfully with username")
        void shouldLoginWithUsername() {
            User user = new User("John Doe", "johndoe", Roles.USER, "john@example.com", null);
            user.setPassword("encoded");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByUsernameOrEmail("johndoe", "johndoe"))
                    .thenReturn(Optional.of(user));
            when(jwt.generateToken(user)).thenReturn("jwt-token");

            Map<String, Object> result = authService.login(loginRequest);

            assertNotNull(result.get("token"));
            assertEquals("jwt-token", result.get("token"));

            AuthResponse authResponse = (AuthResponse) result.get("user");
            assertEquals("johndoe", authResponse.username());
        }

        @Test
        @DisplayName("should login using email when username is blank")
        void shouldLoginWithEmail() {
            loginRequest.setUsername("");
            loginRequest.setEmail("john@example.com");

            User user = new User("John Doe", "johndoe", Roles.USER, "john@example.com", null);
            user.setPassword("encoded");

            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByUsernameOrEmail("john@example.com", "john@example.com"))
                    .thenReturn(Optional.of(user));
            when(jwt.generateToken(user)).thenReturn("jwt-token");

            Map<String, Object> result = authService.login(loginRequest);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should throw 401 when credentials are missing")
        void shouldThrow401WhenCredentialsMissing() {
            loginRequest.setUsername(null);
            loginRequest.setEmail(null);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(loginRequest));

            assertEquals(401, ex.getStatusCode().value());
            assertTrue(ex.getReason().contains("missing"));
        }

        @Test
        @DisplayName("should throw 401 on bad credentials")
        void shouldThrow401OnBadCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(loginRequest));

            assertEquals(401, ex.getStatusCode().value());
        }

        @Test
        @DisplayName("should throw 401 when user not found after authentication")
        void shouldThrow401WhenUserNotFoundAfterAuth() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByUsernameOrEmail("johndoe", "johndoe"))
                    .thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login(loginRequest));

            assertEquals(401, ex.getStatusCode().value());
        }
    }
}
