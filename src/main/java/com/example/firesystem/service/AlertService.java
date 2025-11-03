package com.example.firesystem.service;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.enums.StatusType;
import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.mapper.AlertMapper;
import com.example.firesystem.model.Alert;
import com.example.firesystem.repository.AlertRepository;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public List<AlertDto> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(AlertMapper::alertToAlertDto)
                .toList();
    }

    public List<AlertDto> getAlertsByStatus(StatusType status) {
        return alertRepository.findByStatus(status).stream()
                .map(AlertMapper::alertToAlertDto)
                .toList();
    }

    public AlertDto getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with id " + id + " not found"));
        return AlertMapper.alertToAlertDto(alert);
    }

    public AlertDto assignAlert(Long id, Long userId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with id " + id + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        alert.setAssignedTo(user);

        alert = alertRepository.save(alert);
        return AlertMapper.alertToAlertDto(alert);
    }

    public AlertDto changeStatus(Long id, StatusType status) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with id " + id + " not found"));

        alert.setStatus(status);

        return AlertMapper.alertToAlertDto(alert);
    }

    public AlertDto addPhotoToAlert(Long id, String photoUrl) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with id " + id + " not found"));

        alert.getPhotoUrls().add(photoUrl);

        return AlertMapper.alertToAlertDto(alert);
    }
}