package com.constructionhub.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Authentication Service.
 * 
 * This service manages user authentication, authorization, and security
 * for the Construction Hub system.
 * 
 * EN: This is the entry point for the Authentication Service microservice that handles
 * all security-related operations including login, registration, and permission management.
 * 
 * PT: Este é o ponto de entrada para o microsserviço Authentication Service que gerencia
 * todas as operações relacionadas à segurança, incluindo login, registro e gerenciamento de permissões.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProviderAuth") // <<<--- Referenciando o bean AuditorAware
public class AuthenticationServiceApplication {

    /**
     * Main method that starts the Authentication Service application.
     * 
     * EN: Launches the Spring Boot application for authentication and security management.
     * PT: Inicia a aplicação Spring Boot para gerenciamento de autenticação e segurança.
     * 
     * @param args Command line arguments passed to the application
     */
	public static void main(String[] args) {
		SpringApplication.run(AuthenticationServiceApplication.class, args);
	}
}
