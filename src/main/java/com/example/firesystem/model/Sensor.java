package com.example.firesystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model; // Модель сенсора

    private String location; // Местоположение (например, "Аудитория 101")

    @ManyToOne
    private User assignedTo; // Ответственный пользователь
}