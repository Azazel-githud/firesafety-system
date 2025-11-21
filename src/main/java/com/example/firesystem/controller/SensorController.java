package com.example.firesystem.controller;

import com.example.firesystem.dto.SensorDto;
import com.example.firesystem.mapper.SensorMapper;
import com.example.firesystem.model.Sensor;
import com.example.firesystem.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    public List<SensorDto> getAllSensors() {
        return sensorService.getSensors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorDto> getSensorById(@PathVariable Long id) {
        return ResponseEntity.ok().body(sensorService.getSensorById(id));
    }

    @PostMapping
    public ResponseEntity<SensorDto> createSensor(@RequestBody Sensor sensor) {
        Sensor createdSensor = sensorService.createSensor(sensor);
        return ResponseEntity.ok(SensorMapper.sensorToSensorDto(createdSensor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
        boolean deleted = sensorService.deleteSensor(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}