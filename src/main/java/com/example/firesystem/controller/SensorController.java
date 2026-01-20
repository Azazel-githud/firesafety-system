package com.example.firesystem.controller;

import com.example.firesystem.dto.SensorRequestDto;
import com.example.firesystem.dto.SensorResponseDto;
import com.example.firesystem.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensors", description = "Methods for managing fire system sensors")
public class SensorController {

    private final SensorService sensorService;

    @Operation(summary = "Get All Sensors", description = "Retrieves a list of all sensors")
    @PreAuthorize("hasAnyAuthority('SENSOR_READ', 'USER')")
    @GetMapping
    public ResponseEntity<List<SensorResponseDto>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @Operation(summary = "Get Sensor by ID", description = "Retrieves a specific sensor by its unique identifier")
    @PreAuthorize("hasAnyAuthority('SENSOR_READ', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SensorResponseDto> getSensorById(
            @Parameter(description = "ID of the sensor to retrieve", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sensorService.getSensorById(id));
    }

    @Operation(summary = "Create New Sensor", description = "Creates a new sensor in the system")
    @PreAuthorize("hasAnyAuthority('SENSOR_CREATE', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SensorResponseDto> createSensor(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Sensor data to create", required = true) @Valid @RequestBody SensorRequestDto sensorRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sensorService.createSensor(sensorRequestDto));
    }

    @Operation(summary = "Update Sensor", description = "Updates an existing sensor")
    @PreAuthorize("hasAnyAuthority('SENSOR_UPDATE', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SensorResponseDto> updateSensor(
            @Parameter(description = "ID of the sensor to update", required = true) @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated sensor data", required = true) @Valid @RequestBody SensorRequestDto sensorRequestDto) {
        return ResponseEntity.ok(sensorService.updateSensor(id, sensorRequestDto));
    }

    @Operation(summary = "Delete Sensor", description = "Deletes a sensor from the system")
    @PreAuthorize("hasAnyAuthority('SENSOR_DELETE', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(
            @Parameter(description = "ID of the sensor to delete", required = true) @PathVariable Long id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign Sensor to User", description = "Assigns a sensor to a specific user")
    @PreAuthorize("hasAnyAuthority('SENSOR_ASSIGN', 'ADMIN')")
    @PutMapping("/{id}/assign")
    public ResponseEntity<SensorResponseDto> assignSensor(
            @Parameter(description = "ID of the sensor to assign", required = true) @PathVariable Long id,

            @Parameter(description = "ID of the user to assign the sensor to", required = true) @RequestParam Long userId) {
        return ResponseEntity.ok(sensorService.assignSensor(id, userId));
    }

    @Operation(summary = "Unassign Sensor", description = "Removes assignment from a sensor")
    @PreAuthorize("hasAnyAuthority('SENSOR_ASSIGN', 'ADMIN')")
    @PutMapping("/{id}/unassign")
    public ResponseEntity<SensorResponseDto> unassignSensor(
            @Parameter(description = "ID of the sensor to unassign", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(sensorService.unassignSensor(id));
    }
}