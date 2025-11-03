package com.example.firesystem.model;

import java.time.LocalDateTime;

import com.example.firesystem.enums.TokenType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TokenType type;

    private String value;

    private LocalDateTime expiringDate;

    private boolean disabled;

    @ManyToOne
    private User user;

    public Token(TokenType type, String value, LocalDateTime expiringDate, boolean disabled, User user) {
        this.type = type;
        this.value = value;
        this.expiringDate = expiringDate;
        this.disabled = disabled;
        this.user = user;
    }
}
