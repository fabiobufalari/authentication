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
import org.slf4j.Logger; // Adicionar Logger
import org.slf4j.LoggerFactory; // Adicionar LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException; // Capturar exceção mais específica
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set; // Importar Set

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class); // Logger

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
        log.info("Attempting login for user: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed for user {}: Invalid credentials", request.getUsername());
            throw new ApiException("auth.invalidCredentials", null, HttpStatus.UNAUTHORIZED);
        }

        UserEntity userEntity = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> {
                            // Isso não deveria acontecer se o authenticationManager.authenticate passou
                            log.error("User {} authenticated but not found in repository.", request.getUsername());
                            return new ApiException("auth.userNotFoundAfterAuthentication", null, HttpStatus.INTERNAL_SERVER_ERROR);
                        }));
        log.info("Login successful for user: {}", userEntity.getUsername());
        return jwtTokenProvider.generateTokens(userEntity);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Attempting to register new user with username: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists.", request.getUsername());
            throw new ApiException("auth.userExists", new Object[]{request.getUsername()}, HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", request.getEmail());
            throw new ApiException("auth.emailExists", new Object[]{request.getEmail()}, HttpStatus.CONFLICT);
        }

        RoleEntity userRoleEntity = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Default role ROLE_USER not found during registration.");
                    return new ApiException("role.defaultNotFound", new Object[]{"ROLE_USER"}, HttpStatus.INTERNAL_SERVER_ERROR);
                });

        UserEntity userEntity = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(new HashSet<>(Collections.singletonList(userRoleEntity))) // Nome do campo de roles em UserEntity
                .enabled(true) // Campos de UserDetails
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userEntity = userRepository.save(userEntity);
        log.info("User registered successfully: {}", userEntity.getUsername());
        return jwtTokenProvider.generateTokens(userEntity);
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("Attempting to refresh token.");
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Refresh token validation failed.");
            throw new ApiException("auth.invalidOrExpiredRefreshToken", null, HttpStatus.UNAUTHORIZED);
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        // String userId = jwtTokenProvider.getUserIdFromToken(refreshToken); // Se quiser usar ID para buscar

        UserEntity userEntity = userRepository.findByUsername(username) // Ou findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.warn("User {} not found for refresh token.", username);
                    return new ApiException("user.notFoundFromToken", null, HttpStatus.NOT_FOUND);
                });
        log.info("Token refreshed successfully for user: {}", username);
        return jwtTokenProvider.generateTokens(userEntity);
    }

    public void logout(String token) {
        // A invalidação de JWT geralmente é feita no lado do cliente (removendo o token).
        // Se for necessária uma blacklist no servidor, essa lógica seria implementada aqui
        // (ex: armazenar o JTI - JWT ID - do token em uma lista de tokens revogados até a expiração).
        log.info("User logout processed for token (invalidation typically client-side or via blacklist).");
    }
}