package com.constructionhub.authentication.controller;


import com.constructionhub.authentication.dto.AuthResponseDTO;
import com.constructionhub.authentication.dto.LoginRequestDTO;
import com.constructionhub.authentication.dto.RegisterRequestDTO;
import com.constructionhub.authentication.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API para autenticação de usuários")
public class AuthController {

    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna tokens de acesso")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO authResponseDTO = authService.login(request);
        return ResponseEntity.ok(authResponseDTO);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO authResponseDTO = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova token de acesso usando refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        AuthResponseDTO authResponseDTO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponseDTO);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Sair da sessão", description = "Invalida o token atual")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Formato de token inválido");
        }
        String jwtToken = token.substring(7);
        authService.logout(jwtToken);
        return ResponseEntity.noContent().build();
    }
}