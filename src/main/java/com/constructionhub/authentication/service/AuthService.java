package com.constructionhub.authentication.service;

import com.constructionhub.authentication.dto.AuthResponseDTO;
import com.constructionhub.authentication.dto.LoginRequestDTO;
import com.constructionhub.authentication.dto.RegisterRequestDTO;
import com.constructionhub.authentication.entity.RoleEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.RoleRepository;
import com.constructionhub.authentication.repository.UserRepository;
import com.constructionhub.authentication.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        // Authenticate user using AuthenticationManager
        // Autentica o usuário usando AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Search for user by username or email
        // Procura o usuário pelo username (ou email)
        UserEntity userEntity = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> new ApiException("auth.invalidCredentials", null, HttpStatus.UNAUTHORIZED)));

        // Generate tokens (access and refresh)
        // Gera os tokens (access e refresh)
        return jwtTokenProvider.generateTokens(userEntity);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if username already exists
        // Verifica se o nome de usuário já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("auth.userExists", null, HttpStatus.CONFLICT);
        }

        // Check if email already exists
        // Verifica se o email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("auth.emailExists", null, HttpStatus.CONFLICT);
        }

        // Get default role (changed to "ROLE_USER" to match DataLoader)
        // Obtém a role padrão (alterada para "ROLE_USER" para coincidir com o DataLoader)
        RoleEntity userRoleEntity = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.INTERNAL_SERVER_ERROR));

        // Create new user entity
        // Cria a entidade de usuário
        UserEntity userEntity = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roleEntities(new HashSet<>(Collections.singletonList(userRoleEntity)))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        // Save the new user
        // Salva o novo usuário no repositório
        userEntity = userRepository.save(userEntity);

        // Generate tokens (access and refresh)
        // Gera os tokens (access e refresh)
        return jwtTokenProvider.generateTokens(userEntity);
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        // Validate refresh token
        // Valida o refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ApiException("auth.invalidToken", null, HttpStatus.UNAUTHORIZED);
        }

        // Extract username from token
        // Extrai o username do token
        String username = jwtTokenProvider.getUsername(refreshToken);

        // Find user by username
        // Procura o usuário pelo username
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Generate new tokens (access and refresh)
        // Gera novos tokens (access e refresh)
        return jwtTokenProvider.generateTokens(userEntity);
    }

    public void logout(String token) {
        // Token blacklist can be implemented here to prevent reuse.
        // Implementar blacklist para tokens aqui, se necessário.
    }
}
