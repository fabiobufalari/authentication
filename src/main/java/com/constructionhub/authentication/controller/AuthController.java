package com.constructionhub.authentication.controller;


import com.constructionhub.authentication.dto.AuthResponse;
import com.constructionhub.authentication.dto.LoginRequest;
import com.constructionhub.authentication.dto.RegisterRequest;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova token de acesso usando refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Sair da sessão", description = "Invalida o token atual (opcional)")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7); // Remove "Bearer " prefix
        authService.logout(jwtToken);
        return ResponseEntity.noContent().build();
    }
}