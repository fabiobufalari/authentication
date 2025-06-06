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

/**
 * REST controller for authentication operations.
 * 
 * EN: This controller provides REST API endpoints for user authentication,
 * including login, registration, token refresh, and logout operations.
 * 
 * PT: Este controlador fornece endpoints de API REST para autenticação de usuários,
 * incluindo operações de login, registro, renovação de token e logout.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API para autenticação de usuários")
public class AuthController {

    private final AuthService authService;
    
    /**
     * Constructor for AuthController.
     * 
     * EN: Initializes the controller with the required authentication service.
     * PT: Inicializa o controlador com o serviço de autenticação necessário.
     * 
     * @param authService The authentication service to be used
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Authenticate a user.
     * 
     * EN: Authenticates a user with the provided credentials and returns access tokens.
     * PT: Autentica um usuário com as credenciais fornecidas e retorna tokens de acesso.
     * 
     * @param request The login request containing username and password
     * @return ResponseEntity containing authentication tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna tokens de acesso")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO authResponseDTO = authService.login(request);
        return ResponseEntity.ok(authResponseDTO);
    }
    
    /**
     * Register a new user.
     * 
     * EN: Registers a new user in the system and returns access tokens.
     * PT: Registra um novo usuário no sistema e retorna tokens de acesso.
     * 
     * @param request The registration request containing user details
     * @return ResponseEntity containing authentication tokens with HTTP status 201 (Created)
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário no sistema")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO authResponseDTO = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }
    
    /**
     * Refresh an access token.
     * 
     * EN: Renews an access token using a valid refresh token.
     * PT: Renova um token de acesso usando um refresh token válido.
     * 
     * @param refreshToken The refresh token to use for generating a new access token
     * @return ResponseEntity containing new authentication tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova token de acesso usando refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        AuthResponseDTO authResponseDTO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponseDTO);
    }
    
    /**
     * Logout a user.
     * 
     * EN: Invalidates the current token, effectively logging out the user.
     * PT: Invalida o token atual, efetivamente desconectando o usuário.
     * 
     * @param token The authorization token from the request header
     * @return ResponseEntity with HTTP status 204 (No Content)
     * @throws IllegalArgumentException if the token format is invalid
     */
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
