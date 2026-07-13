package com.javaee.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试（无需 Spring 上下文）
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "test-secret-key-at-least-32-characters-long!!",
                3600000L // 1 小时
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(1L, "demo");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtUtil.generateToken(42L, "testuser");
        assertEquals(42L, jwtUtil.getUserIdFromToken(token));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, "demo");
        assertEquals("demo", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.string"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void shouldRejectExpiredToken() {
        // 创建一个即时过期的 token
        JwtUtil shortLivedJwt = new JwtUtil(
                "test-secret-key-at-least-32-characters-long!!",
                -1L // 立即过期
        );
        String token = shortLivedJwt.generateToken(1L, "demo");
        assertFalse(shortLivedJwt.validateToken(token));
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, "user1");
        String token2 = jwtUtil.generateToken(2L, "user2");
        assertNotEquals(token1, token2);
    }
}
