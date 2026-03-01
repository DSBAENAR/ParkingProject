package com.parking.core.auth.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class BlackListTokenTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private BlackListToken blackListToken;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        blackListToken = new BlackListToken(redisTemplate);
    }

    @Test
    @DisplayName("add - stores token in Redis")
    void add_storesToken() {
        blackListToken.add("token123", 60000L);

        verify(valueOps).set("token123", "revoked", 60000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("isBlackListed - returns true when key exists")
    void isBlackListed_true() {
        when(redisTemplate.hasKey("token123")).thenReturn(true);

        assertTrue(blackListToken.isBlackListed("token123"));
    }

    @Test
    @DisplayName("isBlackListed - returns false when key does not exist")
    void isBlackListed_false() {
        when(redisTemplate.hasKey("token_missing")).thenReturn(false);

        assertFalse(blackListToken.isBlackListed("token_missing"));
    }

    @Test
    @DisplayName("add - Redis exception is handled gracefully")
    void add_redisException() {
        doThrow(new RuntimeException("Redis down")).when(valueOps)
                .set(anyString(), anyString(), eq(60000L), eq(TimeUnit.MILLISECONDS));

        // Should not throw
        assertDoesNotThrow(() -> blackListToken.add("token123", 60000L));
    }

    @Test
    @DisplayName("isBlackListed - Redis exception returns false")
    void isBlackListed_redisException() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

        assertFalse(blackListToken.isBlackListed("token123"));
    }
}
