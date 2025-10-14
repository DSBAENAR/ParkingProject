package com.parking.core.auth.services;

import java.io.IOException;


import org.springframework.web.filter.OncePerRequestFilter;

import com.parking.core.auth.model.BlackListToken;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class JWTAuthFilter extends OncePerRequestFilter{
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final BlackListToken blackListToken;

    

    public JWTAuthFilter(JWTService jwtService, UserDetailsService userDetailsService, BlackListToken blackListToken) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.blackListToken = blackListToken;
    }

    /**
     * Determines whether the filter should not be applied to the given request.
     * This method checks the request's servlet path and excludes requests that
     * start with "/api/v1/parking/auth/" from being filtered.
     *
     * @param request the HttpServletRequest to check
     * @return {@code true} if the request should not be filtered, {@code false} otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/parking/auth/");
    }


    /**
     * Filters incoming HTTP requests to validate JWT tokens and set the authentication context.
     *
     * <p>This method intercepts requests to check for the presence of a valid JWT token in the
     * Authorization header. If a valid token is found, it extracts the username, validates the token,
     * and sets the authentication context in the SecurityContextHolder. If the token is invalid or
     * expired, it responds with a 401 Unauthorized status and an error message in JSON format.</p>
     *
     * @param request     the HTTP request being processed
     * @param response    the HTTP response to be sent
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException if an error occurs during request processing
     * @throws IOException      if an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")){
                    filterChain.doFilter(request, response);
                    return;
                }

                String token = authHeader.substring(7);

                if (blackListToken.isBlackListed(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token revoked\"}");
                    return;
                }

                
                String username = null;

                try {
                    username = jwtService.extractUsername(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                        UserDetails user = userDetailsService.loadUserByUsername(username);
                        if (jwtService.isValidToken(token, user)){
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, user.getAuthorities());
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        }
                    filterChain.doFilter(request, response);

                } catch (JwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                    
                }            
    }

}
 