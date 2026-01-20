package com.example.firesystem.controller;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.dto.AlertRequestDto;
import com.example.firesystem.service.AlertService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import com.example.firesystem.enums.StatusType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class IncidentController {

    private final AlertService alertService;

    @Operation(summary = "Get All Alerts", description = "Retrieves a list of all alerts with optional filtering")
    @PreAuthorize("hasAnyAuthority('ALERT_READ', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<AlertDto>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @Operation(summary = "Get Alert by ID", description = "Retrieves a specific alert by its unique identifier")
    @PreAuthorize("hasAnyAuthority('ALERT_READ', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AlertDto> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @Operation(summary = "Get Alerts by Status", description = "Retrieves a list of alerts by Status")
    @PreAuthorize("hasAnyAuthority('ALERT_READ', 'ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlertDto>> getAlertsByStatus(@PathVariable StatusType status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }

    @Operation(summary = "Get Alerts by Sensor", description = "Retrieves a list of alerts by Sensor")
    @PreAuthorize("hasAnyAuthority('ALERT_READ', 'ADMIN')")
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<List<AlertDto>> getAlertsBySensor(@PathVariable Long sensorId) {
        return ResponseEntity.ok(alertService.getAlertsBySensor(sensorId));
    }

    @Operation(summary = "Create New Alert", description = "Creates a new alert in the system")
    @PreAuthorize("hasAnyAuthority('ALERT_CREATE', 'ADMIN')")
    @PostMapping
    public ResponseEntity<AlertDto> createAlert(@Valid @RequestBody AlertRequestDto alertRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(alertRequestDto));
    }

    @Operation(summary = "Update Alert", description = "Updates an existing alert")
    @PreAuthorize("hasAnyAuthority('ALERT_UPDATE', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AlertDto> updateAlert(@PathVariable Long id,
            @Valid @RequestBody AlertRequestDto alertRequestDto) {
        return ResponseEntity.ok(alertService.updateAlert(id, alertRequestDto));
    }

    @Operation(summary = "Delete Alert", description = "Deletes an alert from the system")
    @PreAuthorize("hasAnyAuthority('ALERT_DELETE', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign Alert to User", description = "Assigns a specific alert to a user for handling")
    @PreAuthorize("hasAnyAuthority('ALERT_ASSIGN', 'ADMIN')")
    @PutMapping("/{id}/assign")
    public ResponseEntity<AlertDto> assignAlert(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(alertService.assignAlert(id, userId));
    }

    @Operation(summary = "Change Alert Status", description = "Updates the status of a specific alert")
    @PreAuthorize("hasAnyAuthority('ALERT_UPDATE', 'ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AlertDto> changeStatus(@PathVariable Long id, @RequestParam StatusType status) {
        return ResponseEntity.ok(alertService.changeStatus(id, status));
    }

    @Operation(summary = "Add Photo to Alert", description = "Adds a photo URL to an existing alert")
    @PreAuthorize("hasAnyAuthority('ALERT_UPDATE', 'ADMIN')")
    @PostMapping("/{id}/photos")
    public ResponseEntity<AlertDto> addPhoto(@PathVariable Long id, @RequestParam String photoUrl) {
        return ResponseEntity.ok(alertService.addPhotoToAlert(id, photoUrl));
    }

    @Operation(summary = "Remove Photo from Alert", description = "Removes a photo URL from an alert")
    @PreAuthorize("hasAnyAuthority('ALERT_UPDATE', 'ADMIN')")
    @DeleteMapping("/{id}/photos")
    public ResponseEntity<AlertDto> removePhoto(@PathVariable Long id, @RequestParam String photoUrl) {
        return ResponseEntity.ok(alertService.removePhotoFromAlert(id, photoUrl));
    }
}