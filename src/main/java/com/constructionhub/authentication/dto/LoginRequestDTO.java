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
public class LoginRequestDTO {

    @NotBlank(message = "Username/Email é obrigatório")
    private String username; // Pode ser username ou email

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}