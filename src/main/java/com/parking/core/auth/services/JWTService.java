package com.parking.core.auth.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.parking.core.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
    private final SecretKey key;
    private final long expirationMs;

    public JWTService(
        @Value("${secret-key}")
        String secretBaese64,
        @Value("${jwt.expiration}")
        long expirationMs
    ){
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBaese64));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the specified user.
     *
     * @param user the user details for whom the token is to be generated
     * @return a JWT token as a String containing the user's roles as claims
     */
    public String generateToken(User user){
        Map<String,Object> extraClaims = Map
        .of("roles",List.of(user.getRole().name()),
            "email", user.getEmail(),
            "username", user.getUsername()
        );
        return generateToken(extraClaims,
        org.springframework.security.core.userdetails.User
        .builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRole().name())
        .build()
        );
    }

    /**
     * Generates a JWT token for the specified user with additional claims.
     *
     * @param extraClaims a map containing extra claims to be included in the token payload
     * @param user the user details for whom the token is generated
     * @return a signed JWT token as a String
     */
    public String generateToken(Map<String,Object> extraClaims, UserDetails user){
        long now = System.currentTimeMillis();
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using the provided resolver function.
     *
     * @param <T>      the type of the claim to be extracted
     * @param token    the JWT token from which to extract the claim
     * @param resolver a function that takes the parsed Claims object and returns the desired claim
     * @return the extracted claim of type T
     */
    private <T> T extractClaim(String token, Function<Claims,T> resolver) {
        Claims claims = Jwts
                            .parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return resolver.apply(claims);
    }

    /**
     * Validates the provided JWT token by checking if the username extracted from the token
     * matches the username of the given user and ensures the token is not expired.
     *
     * @param token the JWT token to validate
     * @param user the user details to compare against the token's username
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise
     */
    public boolean isValidToken(String token, UserDetails user){
        String name = extractUsername(token);
        return name.equals(user.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if the provided JWT token has expired.
     *
     * @param token the JWT token to be checked
     * @return {@code true} if the token has expired, {@code false} otherwise
     */
    private boolean isTokenExpired(String token) {
       Date exp = extractClaim(token, Claims::getExpiration);

       return exp.before(new Date());
    }

    public long getExpiration(String token){
        return extractClaim(token, Claims::getExpiration).getTime() - System.currentTimeMillis();
    }
    
}
