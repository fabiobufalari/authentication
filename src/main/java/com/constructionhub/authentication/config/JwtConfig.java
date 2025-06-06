package com.constructionhub.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Configuration class for JWT authentication settings.
 * 
 * EN: This class provides configuration for JWT token generation, validation,
 * and expiration settings. It also configures the auditor provider for JPA auditing.
 * 
 * PT: Esta classe fornece configuração para geração, validação e configurações
 * de expiração de tokens JWT. Também configura o provedor de auditoria para auditoria JPA.
 */
@Configuration
public class JwtConfig {

    /**
     * Secret key used for signing JWT tokens.
     * 
     * EN: The secret key used to sign and verify JWT tokens.
     * PT: A chave secreta usada para assinar e verificar tokens JWT.
     */
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    /**
     * Validity period for access tokens in milliseconds.
     * 
     * EN: Defines how long an access token remains valid.
     * PT: Define por quanto tempo um token de acesso permanece válido.
     */
    @Value("${security.jwt.token.expire-length}") // Corresponde ao YAML
    private long validityInMilliseconds;

    /**
     * Validity period for refresh tokens in milliseconds.
     * 
     * EN: Defines how long a refresh token remains valid.
     * PT: Define por quanto tempo um token de atualização permanece válido.
     */
    @Value("${security.jwt.refresh-token.expire-length}") // Corresponde ao YAML
    private long refreshValidityInMilliseconds;

    /**
     * Gets the secret key for JWT token operations.
     * 
     * EN: Returns the configured secret key used for JWT operations.
     * PT: Retorna a chave secreta configurada usada para operações JWT.
     * 
     * @return The secret key string
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets the validity period for access tokens.
     * 
     * EN: Returns the configured validity period for access tokens in milliseconds.
     * PT: Retorna o período de validade configurado para tokens de acesso em milissegundos.
     * 
     * @return Validity period in milliseconds
     */
    public long getValidityInMilliseconds() {
        return validityInMilliseconds;
    }

    /**
     * Gets the validity period for refresh tokens.
     * 
     * EN: Returns the configured validity period for refresh tokens in milliseconds.
     * PT: Retorna o período de validade configurado para tokens de atualização em milissegundos.
     * 
     * @return Refresh token validity period in milliseconds
     */
    public long getRefreshValidityInMilliseconds() {
        return refreshValidityInMilliseconds;
    }

    /**
     * Provides an auditor aware implementation for JPA auditing.
     * 
     * EN: Creates a bean that determines the current user for auditing purposes,
     * extracting the username from the security context when available.
     * 
     * PT: Cria um bean que determina o usuário atual para fins de auditoria,
     * extraindo o nome de usuário do contexto de segurança quando disponível.
     * 
     * @return An AuditorAware implementation that provides the current username
     */
    @Bean
    public AuditorAware<String> auditorProviderAuth() { // Renomeado para consistência
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system_auth");
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
