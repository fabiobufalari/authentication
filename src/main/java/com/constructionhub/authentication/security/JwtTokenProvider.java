package com.constructionhub.authentication.security;

import com.constructionhub.authentication.config.JwtConfig;
import com.constructionhub.authentication.dto.AuthResponseDTO;
import com.constructionhub.authentication.entity.RoleEntity; // Ensure RoleEntity is imported
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Add Logger
import org.slf4j.LoggerFactory; // Add Logger
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // Correct import

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class); // Logger instance

    private final JwtConfig jwtConfig;
    private SecretKey secretKey; // Use SecretKey type

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // Initialize the key after properties are injected
    // Inicializa a chave após as propriedades serem injetadas
    @PostConstruct
    public void init() {
        try {
            // Decode secret key from configuration (assuming it's stored securely)
            // Decodifica a chave secreta da configuração (assumindo que está armazenada de forma segura)
            this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
            log.info("JWT Secret Key initialized successfully.");
        } catch (Exception e) {
            log.error("Error initializing JWT Secret Key. Ensure the key is correctly configured and has sufficient length.", e);
            // Handle initialization failure appropriately - perhaps prevent application startup
            // Trate a falha de inicialização apropriadamente - talvez impeça a inicialização da aplicação
            throw new RuntimeException("Failed to initialize JWT Secret Key", e);
        }
    }

    /**
     * Creates an access token for the user.
     * Cria um token de acesso para o usuário.
     *
     * @param username    User's username / Nome de usuário do usuário
     * @param userId      User's UUID / UUID do usuário
     * @param roles       List of user roles / Lista de perfis do usuário
     * @param permissions List of user permissions / Lista de permissões do usuário
     * @return Access Token String / String do Token de Acesso
     */
    public String createToken(String username, String userId, List<String> roles, List<String> permissions) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId); // <<< --- ADDED USER ID CLAIM / ADICIONADO CLAIM userId --- <<<
        claims.put("roles", roles); // Changed key to "roles" for consistency / Chave alterada para "roles" por consistência
        claims.put("permissions", permissions); // Changed key to "permissions" / Chave alterada para "permissions"

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getValidityInMilliseconds());

        log.debug("Creating JWT token for user: {}", username);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256) // Use initialized SecretKey
                .compact();
    }

    /**
     * Creates a refresh token for the user.
     * Cria um refresh token para o usuário.
     *
     * @param username User's username / Nome de usuário do usuário
     * @param userId   User's UUID / UUID do usuário
     * @return Refresh Token String / String do Refresh Token
     */
    public String createRefreshToken(String username, String userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId); // Include userId in refresh token as well / Inclui userId no refresh token também

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getRefreshValidityInMilliseconds());

        log.debug("Creating refresh token for user: {}", username);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256) // Use initialized SecretKey
                .compact();
    }

    /**
     * Generates both access and refresh tokens encapsulated in AuthResponseDTO.
     * Gera ambos os tokens (acesso e refresh) encapsulados em AuthResponseDTO.
     *
     * @param userEntity The user entity / A entidade do usuário
     * @return AuthResponseDTO containing tokens and user details / AuthResponseDTO contendo tokens e detalhes do usuário
     */
    public AuthResponseDTO generateTokens(UserEntity userEntity) {
        List<String> roles = userEntity.getRoleEntities().stream()
                .map(RoleEntity::getName) // Use RoleEntity directly / Use RoleEntity diretamente
                .collect(Collectors.toList());

        List<String> permissions = userEntity.getRoleEntities().stream()
                .flatMap(role -> role.getPermissionEntities().stream())
                .map(permission -> permission.getResource() + ":" + permission.getAction())
                .distinct()
                .collect(Collectors.toList());

        // Pass UUID as String / Passa o UUID como String
        String accessToken = createToken(userEntity.getUsername(), userEntity.getId().toString(), roles, permissions);
        String refreshToken = createRefreshToken(userEntity.getUsername(), userEntity.getId().toString());

        log.info("Tokens generated successfully for user: {}", userEntity.getUsername());
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .roles(roles)
                .build();
    }

    /**
     * Extracts the username (subject) from the token.
     * Extrai o nome de usuário (subject) do token.
     *
     * @param token JWT Token / Token JWT
     * @return Username / Nome de usuário
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the user ID (UUID) from the token claims.
     * Extrai o ID do usuário (UUID) das claims do token.
     *
     * @param token JWT Token / Token JWT
     * @return User ID as String / ID do usuário como String
     */
    public String getUserIdFromToken(String token) {
        return getClaims(token).get("userId", String.class); // Retrieve userId as String
    }


    /**
     * Retrieves all claims from the token.
     * Recupera todas as claims do token.
     *
     * @param token JWT Token / Token JWT
     * @return Claims object / Objeto Claims
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Use initialized SecretKey
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
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new ApiException("auth.tokenSignatureInvalid", null, HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token argument validation failed: {}", e.getMessage());
            throw new ApiException("auth.tokenIllegalArgument", null, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Validates the token signature and expiration.
     * Valida a assinatura e a expiração do token.
     *
     * @param token JWT Token / Token JWT
     * @return true if valid, false otherwise / true se válido, false caso contrário
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Use initialized SecretKey
                    .build()
                    .parseClaimsJws(token);
            // Check expiration
            // Verifica a expiração
            if (claims.getBody().getExpiration().before(new Date())) {
                log.warn("Attempted to validate an expired token for subject: {}", claims.getBody().getSubject());
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log specific exception type for better debugging
            // Loga o tipo específico da exceção para melhor depuração
            log.warn("Token validation failed: {}", e.getClass().getSimpleName(), e);
            return false; // Consider invalid if any exception occurs during parsing/validation
        }
    }
}