package com.collegecrew.service;

import com.collegecrew.dto.LoginRequest;
import com.collegecrew.dto.RegisterRequest;
import com.collegecrew.dto.AuthResponse;
import com.collegecrew.entity.College;
import com.collegecrew.entity.User;
import com.collegecrew.repository.CollegeRepository;
import com.collegecrew.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Register a new user
     * @param registerRequest Registration details
     * @return AuthResponse with token and user details
     * @throws RuntimeException if email already exists
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Extract domain from email
        String domain = extractDomainFromEmail(registerRequest.getEmail());
        
        // Find or create college
        College college = collegeRepository.findByName(domain)
                .orElseGet(() -> {
                    College newCollege = College.builder()
                            .name(domain)
                            .build();
                    return collegeRepository.save(newCollege);
                });

        // Hash password
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Create and save user
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(hashedPassword)
                .alias(registerRequest.getAlias())
                .college(college)
                .build();

        User savedUser = userRepository.save(user);

        // Generate token
        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getAlias(),
                savedUser.getCollege().getId()
        );

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), 
                               savedUser.getAlias(), savedUser.getCollege().getId());
    }

    /**
     * Authenticate user and return token
     * @param loginRequest Login credentials
     * @return AuthResponse with token and user details
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getAlias(),
                user.getCollege().getId()
        );

        return new AuthResponse(token, user.getId(), user.getEmail(), 
                               user.getAlias(), user.getCollege().getId());
    }

    /**
     * Extract domain from email address
     * @param email Email address
     * @return Domain part of the email
     */
    private String extractDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Invalid email format");
        }
        return email.substring(email.indexOf("@") + 1);
    }
}