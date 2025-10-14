package com.parking.core.auth.model;


import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class BlackListToken {
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
        valueOps.set(token,"revoked", expirationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if the given token is blacklisted.
     *
     * @param token the token to check
     * @return {@code true} if the token is blacklisted, {@code false} otherwise
     */
    public boolean isBlackListed(String token){
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}

