package com.example.firesystem.dto;

import com.example.firesystem.enums.EventType;
import com.example.firesystem.enums.StatusType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record AlertDto(
        Long id,
        Long sensorId,
        EventType type,
        LocalDateTime timestamp,
        String description,
        StatusType status,
        List<String> photoUrls
) implements Serializable {
}