package com.example.firesystem.controller;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.service.AlertService;

import jakarta.validation.Valid;

import com.example.firesystem.enums.StatusType;
import com.example.firesystem.model.Alert;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final AlertService alertService;

    // GET /api/incidents?status=new
    @GetMapping
    public List<AlertDto> getIncidents(@RequestParam(required = false) StatusType status) {
        return alertService.getAlertsByStatus(status);
    }

    // GET /api/incidents/{id}
    @GetMapping("/{id}")
    public AlertDto getIncident(@PathVariable Long id) {
        return alertService.getAlertById(id);
    }

    // PUT /api/incidents/{id}/assign
    @PutMapping("/{id}/assign")
    public AlertDto assignIncident(@PathVariable Long id, @RequestParam Long userId) {
        return alertService.assignAlert(id, userId);
    }

    // PUT /api/incidents/{id}/status_change
    @PutMapping("/{id}/status_change")
    public AlertDto changeIncidentStatus(@PathVariable Long id, @RequestParam StatusType status) {
        return alertService.changeStatus(id, status);
    }

    @PostMapping
    public ResponseEntity<Alert> createAlertDto(@RequestBody @Valid Alert alert) {
        Alert newAlert = alertService.create(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAlert);
    }

    // POST /api/incidents/{id}/photos
    @PostMapping("/{id}/photos")
    public AlertDto addPhotoToIncident(@PathVariable Long id, @RequestParam String photoUrl) {
        return alertService.addPhotoToAlert(id, photoUrl);
    }
}