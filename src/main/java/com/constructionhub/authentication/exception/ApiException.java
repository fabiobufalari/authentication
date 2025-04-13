package com.constructionhub.authentication.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for API errors with status codes
 * Exceção personalizada para erros de API com códigos de status
 */
public class ApiException extends RuntimeException {
    
    private final HttpStatus status;
    private final String messageCode;
    private final Object[] messageArgs;
    
    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.messageCode = null;
        this.messageArgs = null;
    }
    
    public ApiException(String messageCode, Object[] messageArgs, HttpStatus status) {
        super(messageCode);
        this.status = status;
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public String getMessageCode() {
        return messageCode;
    }
    
    public Object[] getMessageArgs() {
        return messageArgs;
    }
}