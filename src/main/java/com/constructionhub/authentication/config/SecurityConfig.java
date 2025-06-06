package com.constructionhub.authentication.config;

import com.constructionhub.authentication.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter; // Importe esta classe

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Authentication Service.
 * 
 * EN: This class configures Spring Security settings including JWT authentication,
 * CORS, authorization rules, and password encoding for the Authentication Service.
 * 
 * PT: Esta classe configura as definições de segurança do Spring Security, incluindo
 * autenticação JWT, CORS, regras de autorização e codificação de senha para o Serviço de Autenticação.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for SecurityConfig.
     * 
     * EN: Initializes the security configuration with required dependencies.
     * PT: Inicializa a configuração de segurança com as dependências necessárias.
     * 
     * @param jwtAuthFilter JWT authentication filter
     * @param userDetailsService Service to load user-specific data
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain.
     * 
     * EN: Defines security rules, including public endpoints, protected resources,
     * CORS configuration, and JWT authentication.
     * 
     * PT: Define regras de segurança, incluindo endpoints públicos, recursos protegidos,
     * configuração CORS e autenticação JWT.
     * 
     * @param http HttpSecurity to be configured
     * @return The configured SecurityFilterChain
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/auth/**",
                    "/docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/clients/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            // O ForwardedHeaderFilter geralmente não é adicionado aqui na cadeia SecurityFilterChain
            // Ele deve ser um Bean no contexto da aplicação para ser adicionado
            // no lugar correto na cadeia de filtros padrão do Spring Boot
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates an authentication provider.
     * 
     * EN: Configures the authentication provider with user details service and password encoder.
     * PT: Configura o provedor de autenticação com o serviço de detalhes do usuário e o codificador de senha.
     * 
     * @return Configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates a password encoder.
     * 
     * EN: Provides BCrypt password encoder for secure password hashing.
     * PT: Fornece um codificador de senha BCrypt para hash seguro de senhas.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates an authentication manager.
     * 
     * EN: Provides the authentication manager from the authentication configuration.
     * PT: Fornece o gerenciador de autenticação a partir da configuração de autenticação.
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception If retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures CORS settings.
     * 
     * EN: Defines Cross-Origin Resource Sharing settings for the application.
     * PT: Define as configurações de Compartilhamento de Recursos de Origem Cruzada para a aplicação.
     * 
     * @return Configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    
    /**
     * Creates a forwarded header filter.
     * 
     * EN: Processes X-Forwarded-* headers when the application is behind a proxy.
     * PT: Processa cabeçalhos X-Forwarded-* quando a aplicação está atrás de um proxy.
     * 
     * @return ForwardedHeaderFilter instance
     */
    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
