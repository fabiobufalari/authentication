package com.constructionhub.authentication.service;


import com.constructionhub.authentication.dto.AuthResponse;
import com.constructionhub.authentication.dto.LoginRequest;
import com.constructionhub.authentication.dto.RegisterRequest;
import com.constructionhub.authentication.entity.Role;
import com.constructionhub.authentication.entity.User;
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

    public AuthResponse login(LoginRequest request) {
        // Autenticar usuário com AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Procurar o usuário pelo username (pode ser username ou email)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> new ApiException("auth.invalidCredentials", null, HttpStatus.UNAUTHORIZED)));

        // Gerar tokens
        return jwtTokenProvider.generateTokens(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar se o usuário já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("auth.userExists", null, HttpStatus.CONFLICT);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("auth.emailExists", null, HttpStatus.CONFLICT);
        }

        // Obter o role padrão (USER)
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.INTERNAL_SERVER_ERROR));

        // Criar o usuário
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        // Salvar usuário
        user = userRepository.save(user);

        // Gerar tokens
        return jwtTokenProvider.generateTokens(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        // Validar refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ApiException("auth.invalidToken", null, HttpStatus.UNAUTHORIZED);
        }

        // Extrair username do token
        String username = jwtTokenProvider.getUsername(refreshToken);

        // Procurar o usuário
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Gerar novos tokens
        return jwtTokenProvider.generateTokens(user);
    }

    public void logout(String token) {
        // Token blacklist poderia ser implementado aqui para impedir reuso
        // Isso requer um repositório para armazenar tokens invalidados
    }
}