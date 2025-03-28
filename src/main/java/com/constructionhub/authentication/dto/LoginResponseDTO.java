package com.constructionhub.authentication.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String name;
    private String role;

    public LoginResponseDTO(String token, String name, String role) {
        this.token = token;
        this.name = name;
        this.role = role;
    }

    // getters
    public String getToken() { return token; }
    public String getName() { return name; }
    public String getRole() { return role; }
}