package com.example.firesystem.dto;

import java.io.Serializable;

public record RegisterRequest(
        String username,
        String password) implements Serializable {
}