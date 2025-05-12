package com.constructionhub.authentication.security;

import com.constructionhub.authentication.config.JwtConfig;
import com.constructionhub.authentication.dto.AuthResponseDTO;
import com.constructionhub.authentication.entity.PermissionEntity; // Importar PermissionEntity
import com.constructionhub.authentication.entity.RoleEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtConfig jwtConfig;
    private SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() {
        try {
            this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
            log.info("JWT Secret Key initialized successfully.");
        } catch (Exception e) {
            log.error("Error initializing JWT Secret Key. Ensure the key is correctly configured and has sufficient length.", e);
            throw new RuntimeException("Failed to initialize JWT Secret Key", e);
        }
    }

    public String createToken(String username, String userId, List<String> roles, List<String> permissions) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId); // Adiciona userId como String
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getValidityInMilliseconds());

        log.debug("Creating JWT access token for user: {}, userId: {}", username, userId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username, String userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId); // Adiciona userId como String

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getRefreshValidityInMilliseconds());

        log.debug("Creating JWT refresh token for user: {}, userId: {}", username, userId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthResponseDTO generateTokens(UserEntity userEntity) {
        List<String> roleNames = userEntity.getRoles().stream() // Nome da coleção de roles em UserEntity
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        // Coletar nomes das permissões diretamente
        List<String> permissionNames = userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()) // Nome da coleção de permissions em RoleEntity
                .map(PermissionEntity::getName) // Mapeia para o nome da permissão
                .distinct()
                .collect(Collectors.toList());

        String userIdStr = userEntity.getId().toString();
        String accessToken = createToken(userEntity.getUsername(), userIdStr, roleNames, permissionNames);
        String refreshToken = createRefreshToken(userEntity.getUsername(), userIdStr);

        log.info("Tokens generated successfully for user: {}", userEntity.getUsername());
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .roles(roleNames)
                // .permissions(permissionNames) // Opcional, se AuthResponseDTO tiver campo para permissions
                .build();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getUserIdFromToken(String token) { // Retorna String
        return getClaims(token).get("userId", String.class);
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new ApiException("auth.tokenExpired", null, HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new ApiException("auth.tokenUnsupported", null, HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new ApiException("auth.tokenMalformed", null, HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.security.SignatureException e) { // Exceção correta para falha de assinatura
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new ApiException("auth.tokenSignatureInvalid", null, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token argument validation failed: {}", e.getMessage());
            throw new ApiException("auth.tokenIllegalArgument", null, HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            if (claimsJws.getBody().getExpiration().before(new Date())) {
                log.warn("Attempted to validate an expired token for subject: {}", claimsJws.getBody().getSubject());
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getClass().getSimpleName(), e.getMessage()); // Simplificado log
            return false;
        }
    }
}