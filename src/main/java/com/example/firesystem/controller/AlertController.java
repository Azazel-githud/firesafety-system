package com.example.firesystem.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.firesystem.model.Alert;
import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.service.AlertService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping()
    public ResponseEntity<List<AlertDto>> getAllAlerts() {
        List<AlertDto> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertDto> getAlertById(@PathVariable Long id) {
        Optional<AlertDto> alert = alertService.getAlertById(id);
        return alert.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/new")
    public ResponseEntity<List<AlertDto>> getNewAlerts() {
        List<AlertDto> newAlerts = alertService.getNewAlerts();
        return ResponseEntity.ok(newAlerts);
    }

    @PostMapping()
    public ResponseEntity<Alert> createAlert(@Valid @RequestBody Alert alert) {
        Alert createdAlert = alertService.createAlert(alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlert);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Alert> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Alert updatedAlert = alertService.updateAlertStatus(id, status);
        if (updatedAlert != null) {
            return ResponseEntity.ok(updatedAlert);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        boolean deleted = alertService.deleteAlert(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Alert>> searchByLocation(
            @RequestParam String location) {

        List<Alert> alerts = alertService.searchByLocation(location);
        return ResponseEntity.ok(alerts);
    }
}