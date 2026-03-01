package com.parking.core.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.parking.core.auth.model.BlackListToken;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JWTAuthFilterTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private BlackListToken blackListToken;

    @Mock
    private FilterChain filterChain;

    private JWTAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JWTAuthFilter(jwtService, userDetailsService, blackListToken);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("shouldNotFilter - auth path returns true")
    void shouldNotFilter_authPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/parking/auth/login");
        assertTrue(jwtAuthFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter - webhook path returns true")
    void shouldNotFilter_webhookPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/webhooks/stripe");
        assertTrue(jwtAuthFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("shouldNotFilter - other path returns false")
    void shouldNotFilter_otherPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/parking/vehicles/");
        assertFalse(jwtAuthFilter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("doFilterInternal - no auth header continues chain")
    void doFilter_noHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - blacklisted token returns 401")
    void doFilter_blacklistedToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer blacklisted_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(blackListToken.isBlackListed("blacklisted_token")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - valid token sets authentication")
    void doFilter_validToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(blackListToken.isBlackListed("valid_token")).thenReturn(false);
        when(jwtService.extractUsername("valid_token")).thenReturn("john");

        UserDetails userDetails = User.builder()
                .username("john")
                .password("encoded")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtService.isValidToken("valid_token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("john", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("doFilterInternal - invalid token returns 401")
    void doFilter_invalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(blackListToken.isBlackListed("bad_token")).thenReturn(false);
        when(jwtService.extractUsername("bad_token")).thenThrow(new JwtException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }
}
