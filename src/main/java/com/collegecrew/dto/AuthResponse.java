package com.collegecrew.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String alias;
    private Long collegeId;
    
    public AuthResponse(String token, Long userId, String email, String alias, Long collegeId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.alias = alias;
        this.collegeId = collegeId;
    }
}