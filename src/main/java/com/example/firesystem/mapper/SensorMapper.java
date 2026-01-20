package com.example.firesystem.mapper;

import com.example.firesystem.dto.SensorResponseDto;
import com.example.firesystem.model.Sensor;

public class SensorMapper {
    public static SensorResponseDto sensorToSensorDto(Sensor sensor) {
        Long userId = (sensor.getAssignedTo() != null) ? sensor.getAssignedTo().getId() : null;
        return new SensorResponseDto(
                sensor.getId(),
                sensor.getModel(),
                sensor.getLocation(),
                userId);
    }
}