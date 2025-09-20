package com.collegecrew.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJwtServiceTesting123456789",
    "jwt.expiration=86400000"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private Long userId;
    private String email;
    private String alias;
    private Long collegeId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        email = "test@test.com";
        alias = "testuser";
        collegeId = 10L;
    }

    @Test
    void testGenerateToken() {
        // When
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testValidateToken() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When & Then
        assertTrue(jwtService.validateToken(token));
        assertFalse(jwtService.validateToken("invalid.token.here"));
        assertFalse(jwtService.validateToken(""));
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void testExtractEmail() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractAllClaims() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        Claims claims = jwtService.extractAllClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(alias, claims.get("alias", String.class));
        assertEquals(collegeId, claims.get("collegeId", Long.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testExtractUserId() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        Long extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractAlias() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        String extractedAlias = jwtService.extractAlias(token);

        // Then
        assertEquals(alias, extractedAlias);
    }

    @Test
    void testExtractCollegeId() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        Long extractedCollegeId = jwtService.extractCollegeId(token);

        // Then
        assertEquals(collegeId, extractedCollegeId);
    }

    @Test
    void testExtractExpiration() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        java.util.Date expiration = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new java.util.Date()));
    }

    @Test
    void testIsTokenExpired() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When & Then
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testTokenWithDifferentUserDetails() {
        // Given
        Long userId2 = 2L;
        String email2 = "another@test.com";
        String alias2 = "anotheruser";
        Long collegeId2 = 20L;

        // When
        String token1 = jwtService.generateToken(userId, email, alias, collegeId);
        String token2 = jwtService.generateToken(userId2, email2, alias2, collegeId2);

        // Then
        assertNotEquals(token1, token2);
        
        // Verify first token claims
        assertEquals(email, jwtService.extractEmail(token1));
        assertEquals(userId, jwtService.extractUserId(token1));
        assertEquals(alias, jwtService.extractAlias(token1));
        assertEquals(collegeId, jwtService.extractCollegeId(token1));
        
        // Verify second token claims
        assertEquals(email2, jwtService.extractEmail(token2));
        assertEquals(userId2, jwtService.extractUserId(token2));
        assertEquals(alias2, jwtService.extractAlias(token2));
        assertEquals(collegeId2, jwtService.extractCollegeId(token2));
    }

    @Test
    void testTokenValidationWithManipulatedToken() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);
        String manipulatedToken = token.substring(0, token.length() - 5) + "abcde";

        // When & Then
        assertTrue(jwtService.validateToken(token));
        assertFalse(jwtService.validateToken(manipulatedToken));
    }

    @Test
    void testExtractClaimWithCustomFunction() {
        // Given
        String token = jwtService.generateToken(userId, email, alias, collegeId);

        // When
        java.util.Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertNotNull(issuedAt);
        assertEquals(email, subject);
        assertTrue(issuedAt.before(new java.util.Date()) || issuedAt.equals(new java.util.Date()));
    }
}