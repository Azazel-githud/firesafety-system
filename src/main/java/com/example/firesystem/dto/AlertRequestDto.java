package com.example.firesystem.dto;

import java.util.List;

import com.example.firesystem.enums.EventType;
import com.example.firesystem.enums.StatusType;

public record AlertRequestDto(
        Long sensorId,
        EventType type,
        String description,
        StatusType status,
        List<String> photoUrl,
        Long userId) {

}
