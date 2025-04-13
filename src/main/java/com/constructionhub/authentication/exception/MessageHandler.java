package com.constructionhub.authentication.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Component for handling message resolution and internationalization
 * Componente para tratamento de resolução e internacionalização de mensagens
 */
@Component
public class MessageHandler {
    
    private final MessageSource messageSource;
    
    public MessageHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Get a message from messages.properties for a given code
     * Obtém uma mensagem do arquivo messages.properties para um código específico
     * 
     * @param code The message code / O código da mensagem
     * @param args Arguments for the message / Argumentos para a mensagem
     * @return The resolved message / A mensagem resolvida
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * Get a message for HTTP status code
     * Obtém uma mensagem para um código de status HTTP
     * 
     * @param statusCode The HTTP status code / O código de status HTTP
     * @return The appropriate message / A mensagem apropriada
     */
    public String getHttpStatusMessage(int statusCode) {
        return getMessage("http.status." + statusCode);
    }
}