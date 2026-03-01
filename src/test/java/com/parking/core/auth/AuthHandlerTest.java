package com.parking.core.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.model.BlackListToken;
import com.parking.core.auth.model.Response.AuthResponse;
import com.parking.core.auth.services.AuthService;
import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.auth.services.JWTService;

@WebMvcTest(AuthHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private BlackListToken blackListToken;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("POST /signUp - success")
    void signUp_success() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("user", new AuthResponse("John", "john@test.com", "john", "USER"));
        response.put("token", "jwt_token");
        when(authService.signUp(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John","username":"john","email":"john@test.com","password":"Password1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.token").value("jwt_token"));
    }

    @Test
    @DisplayName("POST /signUp/bulk - all success")
    void signUpBulk_success() throws Exception {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("user", new AuthResponse("A", "a@t.com", "a", "USER"));
        r1.put("token", "t1");
        Map<String, Object> r2 = new HashMap<>();
        r2.put("user", new AuthResponse("B", "b@t.com", "b", "USER"));
        r2.put("token", "t2");

        when(authService.signUp(any()))
                .thenReturn(r1)
                .thenReturn(r2);

        mockMvc.perform(post("/api/v1/parking/auth/signUp/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {"name":"A","username":"a","email":"a@t.com","password":"Password1"},
                                    {"name":"B","username":"b","email":"b@t.com","password":"Password2"}
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.results.length()").value(2));
    }

    @Test
    @DisplayName("POST /signUp/bulk - partial failure (ResponseStatusException)")
    void signUpBulk_partialFail() throws Exception {
        Map<String, Object> r1 = new HashMap<>();
        r1.put("user", new AuthResponse("A", "a@t.com", "a", "USER"));
        r1.put("token", "t1");

        when(authService.signUp(any()))
                .thenReturn(r1)
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate user"));

        mockMvc.perform(post("/api/v1/parking/auth/signUp/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {"name":"A","username":"a","email":"a@t.com","password":"Password1"},
                                    {"name":"B","username":"b","email":"b@t.com","password":"Password2"}
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.results[1].status").value(400));
    }

    @Test
    @DisplayName("POST /signUp/bulk - runtime error")
    void signUpBulk_runtimeError() throws Exception {
        when(authService.signUp(any()))
                .thenThrow(new RuntimeException("Unexpected DB error"));

        mockMvc.perform(post("/api/v1/parking/auth/signUp/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {"name":"A","username":"a","email":"a@t.com","password":"Password1"}
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value(500))
                .andExpect(jsonPath("$.results[0].message").value("Unexpected error: Unexpected DB error"));
    }

    @Test
    @DisplayName("POST /login - success")
    void login_success() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("user", new AuthResponse("John", "john@test.com", "john", "USER"));
        response.put("token", "jwt_login");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john","password":"Password1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged-in correctly"))
                .andExpect(jsonPath("$.token").value("jwt_login"));
    }

    @Test
    @DisplayName("POST /logout - with Bearer header")
    void logout_withHeader() throws Exception {
        when(jwtService.getExpiration(anyString())).thenReturn(60000L);

        mockMvc.perform(post("/api/v1/parking/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer some_jwt_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successfully"));

        verify(blackListToken).add("some_jwt_token", 60000L);
    }

    @Test
    @DisplayName("POST /logout - without Bearer prefix")
    void logout_withoutBearerPrefix() throws Exception {
        mockMvc.perform(post("/api/v1/parking/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Basic credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successfully"));

        verify(blackListToken, never()).add(anyString(), anyLong());
    }

    @Test
    @DisplayName("POST /logout - null-like header (no Bearer)")
    void logout_nonBearerHeader() throws Exception {
        mockMvc.perform(post("/api/v1/parking/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Token xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successfully"));

        verify(blackListToken, never()).add(anyString(), anyLong());
    }
}
