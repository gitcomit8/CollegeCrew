package com.collegecrew.controller;

import com.collegecrew.dto.AuthResponse;
import com.collegecrew.dto.LoginRequest;
import com.collegecrew.dto.RegisterRequest;
import com.collegecrew.entity.College;
import com.collegecrew.entity.User;
import com.collegecrew.repository.CollegeRepository;
import com.collegecrew.repository.UserRepository;
import com.collegecrew.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJwtServiceTesting123456789",
    "jwt.expiration=86400000"
})
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        // Set up MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up data
        userRepository.deleteAll();
        collegeRepository.deleteAll();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("john@university.edu");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setAlias("johndoe");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@university.edu");
        validLoginRequest.setPassword("password123");
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("john@university.edu"))
                .andExpect(jsonPath("$.alias").value("johndoe"))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.collegeId").exists())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        // Verify user was created
        User savedUser = userRepository.findByEmail("john@university.edu").orElse(null);
        assertNotNull(savedUser);
        assertEquals("john@university.edu", savedUser.getEmail());
        assertEquals("johndoe", savedUser.getAlias());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPasswordHash()));

        // Verify college was created
        College savedCollege = collegeRepository.findByName("university.edu").orElse(null);
        assertNotNull(savedCollege);
        assertEquals("university.edu", savedCollege.getName());
        assertEquals(savedCollege.getId(), savedUser.getCollege().getId());

        // Verify token is valid
        assertTrue(jwtService.validateToken(authResponse.getToken()));
        assertEquals("john@university.edu", jwtService.extractEmail(authResponse.getToken()));
        assertEquals(savedUser.getId(), jwtService.extractUserId(authResponse.getToken()));
        assertEquals("johndoe", jwtService.extractAlias(authResponse.getToken()));
        assertEquals(savedCollege.getId(), jwtService.extractCollegeId(authResponse.getToken()));
    }

    @Test
    void testRegisterWithExistingCollege() throws Exception {
        // Given - College already exists
        College existingCollege = College.builder()
                .name("university.edu")
                .build();
        existingCollege = collegeRepository.save(existingCollege);

        // When
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collegeId").value(existingCollege.getId()));

        // Then - Should use existing college
        assertEquals(1, collegeRepository.count());
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // Given - User already exists
        College college = College.builder().name("university.edu").build();
        college = collegeRepository.save(college);

        User existingUser = User.builder()
                .email("john@university.edu")
                .passwordHash(passwordEncoder.encode("somepassword"))
                .alias("existing")
                .college(college)
                .build();
        userRepository.save(existingUser);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given - User exists
        College college = College.builder().name("university.edu").build();
        college = collegeRepository.save(college);

        User user = User.builder()
                .email("john@university.edu")
                .passwordHash(passwordEncoder.encode("password123"))
                .alias("johndoe")
                .college(college)
                .build();
        user = userRepository.save(user);

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("john@university.edu"))
                .andExpect(jsonPath("$.alias").value("johndoe"))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.collegeId").value(college.getId()))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        // Verify token is valid
        assertTrue(jwtService.validateToken(authResponse.getToken()));
        assertEquals("john@university.edu", jwtService.extractEmail(authResponse.getToken()));
    }

    @Test
    void testLoginInvalidEmail() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("nonexistent@university.edu");
        invalidRequest.setPassword("password123");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        // Given - User exists with different password
        College college = College.builder().name("university.edu").build();
        college = collegeRepository.save(college);

        User user = User.builder()
                .email("john@university.edu")
                .passwordHash(passwordEncoder.encode("differentpassword"))
                .alias("johndoe")
                .college(college)
                .build();
        userRepository.save(user);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDomainExtraction() throws Exception {
        // Test various email domains
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("student@mit.edu");
        request1.setPassword("password123");
        request1.setAlias("student1");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Verify college was created with correct domain
        College college = collegeRepository.findByName("mit.edu").orElse(null);
        assertNotNull(college);
        assertEquals("mit.edu", college.getName());
    }
}