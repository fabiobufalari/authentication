package com.constructionhub.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Para type safety

import java.util.Optional;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @Value("${security.jwt.refresh-token.expire-length}")
    private long refreshValidityInMilliseconds;

    public String getSecretKey() {
        return secretKey;
    }

    public long getValidityInMilliseconds() {
        return validityInMilliseconds;
    }

    public long getRefreshValidityInMilliseconds() {
        return refreshValidityInMilliseconds;
    }

    // O AuditorAware bean já está aqui. A classe AuthenticationServiceApplication tem @EnableJpaAuditing.
    // Não precisa de auditorAwareRef se só houver um bean AuditorAware.
    // Se houver múltiplos, o nome do bean aqui ("auditorProviderAuth" por exemplo) precisaria ser referenciado.
    @Bean
    public AuditorAware<String> auditorProviderAuth() { // Nome do bean alterado para auditorProviderAuth
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system_auth"); // Usuário de sistema específico para auth-service
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return Optional.of(((UserDetails) principal).getUsername());
            } else if (principal instanceof String) {
                return Optional.of((String) principal);
            }
            return Optional.of("unknown_auth_user");
        };
    }
}