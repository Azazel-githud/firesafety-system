package com.example.firesystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model; // Модель сенсора

    private String location; // Местоположение (например, "Аудитория 101")

    @ManyToOne
    private User assignedTo; // Ответственный пользователь
}