package com.constructionhub.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private UUID userId;        // <<< ADICIONADO
    private String username;    // <<< ADICIONADO
    private String email;       // <<< ADICIONADO
    private String firstName;   // <<< ADICIONADO
    private String lastName;    // <<< ADICIONADO
    private List<String> roles; // <<< ADICIONADO (nomes das roles)
    // private List<String> permissions; // Opcional: se quiser retornar permissões explícitas
}