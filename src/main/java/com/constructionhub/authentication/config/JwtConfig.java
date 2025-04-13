package com.constructionhub.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(authentication.getName());
        };
    }
}