package com.constructionhub.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{validation.notBlank}")
    private String username; // Pode ser username ou email

    @NotBlank(message = "{validation.notBlank}")
    private String password;
}