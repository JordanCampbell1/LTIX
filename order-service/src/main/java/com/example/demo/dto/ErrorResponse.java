package com.example.demo.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String message,
    int status,
    Instant timestamp,
    List<String> errors
) {
    public ErrorResponse(String message, int status, List<String> errors) {
        this(message, status, Instant.now(), errors);
    }
    
    public ErrorResponse(String message, int status) {
        this(message, status, Instant.now(), null);
    }
}
