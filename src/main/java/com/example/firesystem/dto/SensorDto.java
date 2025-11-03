package com.example.firesystem.dto;

import java.io.Serializable;

public record SensorDto(
        Long id,
        String model,
        String location,
        Long assignedToUserId) implements Serializable {
}