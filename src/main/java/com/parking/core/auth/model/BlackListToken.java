package com.parking.core.auth.model;


import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * Redis-backed component for blacklisting revoked JWT tokens.
 * <p>
 * When a user logs out, their token is stored in Redis with its remaining
 * time-to-live. Subsequent requests presenting a blacklisted token are rejected
 * by {@link com.parking.core.auth.services.JWTAuthFilter}.
 * </p>
 */
@Component
public class BlackListToken {
    private static final Logger log = LoggerFactory.getLogger(BlackListToken.class);
    private final RedisTemplate<String,String> redisTemplate;
    private final ValueOperations<String,String> valueOps;

    /**
     * Constructs a BlackListToken instance with the specified RedisTemplate.
     *
     * @param redisTemplate the RedisTemplate used for interacting with Redis.
     *                       It provides operations for working with key-value pairs.
     */
    public BlackListToken(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    /**
     * Adds a token to the blacklist with a specified expiration time.
     *
     * @param token        The token to be added to the blacklist.
     * @param expirationMs The expiration time in milliseconds after which the token will be removed from the blacklist.
     */
    public void add(String token, long expirationMs){
        try {
            valueOps.set(token, "revoked", expirationMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Redis unavailable: token blacklisting skipped. Token will expire naturally.", e);
        }
    }

    /**
     * Checks if the given token is blacklisted.
     *
     * @param token the token to check
     * @return {@code true} if the token is blacklisted, {@code false} otherwise
     */
    public boolean isBlackListed(String token){
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(token));
        } catch (Exception e) {
            log.warn("Redis unavailable: cannot check blacklist, treating token as valid.", e);
            return false;
        }
    }
}

