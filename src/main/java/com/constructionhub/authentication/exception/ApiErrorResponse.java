package com.constructionhub.authentication.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Standard error response for API exceptions.
 * Resposta de erro padrão para exceções da API.
 */
public class ApiErrorResponse {

    private int status;
    private String error;
    private String message;
    private Object details; // Additional error details / Detalhes adicionais do erro

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Default constructor initializing timestamp
    // Construtor padrão que inicializa o timestamp
    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiErrorResponse(int status, String error, String message, Object details) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    // Getters and setters / Getters e Setters

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
