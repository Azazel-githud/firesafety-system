package com.example.firesystem.dto;

import java.io.Serializable;

public record SensorRequestDto(
                String model,
                String location,
                Long userId) implements Serializable {
}
