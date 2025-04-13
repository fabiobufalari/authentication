package com.constructionhub.authentication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API
 * Manipulador global de exceções para a API
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final MessageHandler messageHandler;
    
    public GlobalExceptionHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    /**
     * Handle custom API exceptions
     * Tratamento de exceções personalizadas da API
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
        String message;
        
        if (ex.getMessageCode() != null) {
            message = messageHandler.getMessage(ex.getMessageCode(), ex.getMessageArgs());
        } else {
            message = ex.getMessage();
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message", message);
        
        return new ResponseEntity<>(body, ex.getStatus());
    }
    
    /**
     * Handle authentication exceptions
     * Tratamento de exceções de autenticação
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", messageHandler.getMessage("auth.invalidCredentials"));
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle access denied exceptions
     * Tratamento de exceções de acesso negado
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        body.put("message", messageHandler.getMessage("auth.accessDenied"));
        
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle validation exceptions
     * Tratamento de exceções de validação
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", messageHandler.getMessage("validation.error"));
        body.put("details", errors);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle all other exceptions
     * Tratamento de todas as outras exceções
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", messageHandler.getMessage("error.internal"));
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}