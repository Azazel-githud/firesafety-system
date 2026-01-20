package com.example.firesystem.dto;

import java.io.Serializable;

public record SensorResponseDto(
                Long id,
                String model,
                String location,
                Long assignedToUserId) implements Serializable {
}