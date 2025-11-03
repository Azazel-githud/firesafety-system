package com.example.firesystem.mapper;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.model.Alert;

public class AlertMapper {
    public static AlertDto alertToAlertDto(Alert alert) {
        return new AlertDto(
                alert.getId(),
                alert.getSensor().getId(),
                alert.getType(),
                alert.getTimestamp(),
                alert.getDescription(),
                alert.getStatus(),
                alert.getPhotoUrls());
    }
}