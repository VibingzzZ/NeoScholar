package com.javaee.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-at-least-32-characters-long!!", 3600000L);
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
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, "user1");
        String token2 = jwtUtil.generateToken(2L, "user2");
        assertNotEquals(token1, token2);
    }
}
