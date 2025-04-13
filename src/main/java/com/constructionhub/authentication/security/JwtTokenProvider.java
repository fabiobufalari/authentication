package com.constructionhub.authentication.security;

import com.constructionhub.authentication.config.JwtConfig;
import com.constructionhub.authentication.dto.AuthResponse;
import com.constructionhub.authentication.entity.User;
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
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        
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
    
    public AuthResponse generateTokens(User user) {
        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toList());
            
        List<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getResource() + ":" + permission.getAction())
            .distinct()
            .collect(Collectors.toList());
            
        String accessToken = createToken(user.getUsername(), roles, permissions);
        String refreshToken = createRefreshToken(user.getUsername());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
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