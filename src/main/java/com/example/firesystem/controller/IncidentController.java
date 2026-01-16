package com.example.firesystem.controller;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.service.AlertService;

import jakarta.validation.Valid;

import com.example.firesystem.enums.StatusType;
import com.example.firesystem.model.Alert;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final AlertService alertService;

    @PreAuthorize("hasAuthority('SENSOR_READ')")
    @GetMapping
    public List<AlertDto> getIncidents(@RequestParam(required = false) StatusType status) {
        return alertService.getAlertsByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertDto> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok().body(alertService.getAlertById(id));
    }

    @PutMapping("/{id}/assign")
    public AlertDto assignIncident(@PathVariable Long id, @RequestParam Long userId) {
        return alertService.assignAlert(id, userId);
    }

    @PutMapping("/{id}/status_change")
    public AlertDto changeIncidentStatus(@PathVariable Long id, @RequestParam StatusType status) {
        return alertService.changeStatus(id, status);
    }

    @PostMapping
    public ResponseEntity<Alert> createAlertDto(@RequestBody @Valid Alert alert) {
        Alert newAlert = alertService.create(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAlert);
    }

    @PostMapping("/{id}/photos")
    public AlertDto addPhotoToIncident(@PathVariable Long id, @RequestParam String photoUrl) {
        return alertService.addPhotoToAlert(id, photoUrl);
    }
}