package com.constructionhub.authentication.security;

import com.constructionhub.authentication.config.JwtConfig;
import com.constructionhub.authentication.dto.AuthResponseDTO;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenProvider {
    
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    
    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
    
    public String createToken(String username, List<String> roles, List<String> permissions) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roleEntities", roles);
        claims.put("permissionEntities", permissions);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getValidityInMilliseconds());
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfig.getRefreshValidityInMilliseconds());
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    public AuthResponseDTO generateTokens(UserEntity userEntity) {
        List<String> roles = userEntity.getRoleEntities().stream()
            .map(role -> role.getName())
            .collect(Collectors.toList());
            
        List<String> permissions = userEntity.getRoleEntities().stream()
            .flatMap(role -> role.getPermissionEntities().stream())
            .map(permission -> permission.getResource() + ":" + permission.getAction())
            .distinct()
            .collect(Collectors.toList());
            
        String accessToken = createToken(userEntity.getUsername(), roles, permissions);
        String refreshToken = createRefreshToken(userEntity.getUsername());
        
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
    
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException("auth.invalidToken", null, HttpStatus.UNAUTHORIZED);
        }
    }


}