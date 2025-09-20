package com.collegecrew.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Test
    void testPasswordEncoderBeanExists() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder);
        // Verify it's BCrypt by checking the encoded password starts with $2
        assertTrue(passwordEncoder.encode("test").startsWith("$2"));
    }

    @Test
    void testCorsConfigurationSourceBeanExists() {
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        assertNotNull(corsConfigurationSource);
        
        // Verify CORS configuration source is properly configured
        assertTrue(corsConfigurationSource instanceof org.springframework.web.cors.UrlBasedCorsConfigurationSource);
    }
}