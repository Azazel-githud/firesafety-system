package com.example.firesystem.dto;

import java.time.LocalDateTime;
import java.io.Serializable;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) implements Serializable {
}