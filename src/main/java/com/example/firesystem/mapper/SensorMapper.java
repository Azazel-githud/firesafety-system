package com.example.firesystem.mapper;

import com.example.firesystem.dto.SensorDto;
import com.example.firesystem.model.Sensor;

public class SensorMapper {
    public static SensorDto sensorToSensorDto(Sensor sensor) {
        Long userId = (sensor.getAssignedTo() != null) ? sensor.getAssignedTo().getId() : null;
        return new SensorDto(
                sensor.getId(),
                sensor.getModel(),
                sensor.getLocation(),
                userId);
    }
}