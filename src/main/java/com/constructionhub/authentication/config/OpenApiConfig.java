package com.constructionhub.authentication.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation.
 * 
 * EN: This class configures the OpenAPI (Swagger) documentation for the Authentication Service,
 * including API information, security schemes, and contact details.
 * 
 * PT: Esta classe configura a documentação OpenAPI (Swagger) para o Serviço de Autenticação,
 * incluindo informações da API, esquemas de segurança e detalhes de contato.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates a custom OpenAPI configuration.
     * 
     * EN: Defines the OpenAPI documentation with title, description, version,
     * contact information, and security requirements.
     * 
     * PT: Define a documentação OpenAPI com título, descrição, versão,
     * informações de contato e requisitos de segurança.
     * 
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Autenticação e Autorização")
                        .description("Microserviço para gerenciamento de autenticação e autorização")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@example.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
