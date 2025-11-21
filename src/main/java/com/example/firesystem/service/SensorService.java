package com.example.firesystem.service;

import com.example.firesystem.dto.SensorDto;
import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.mapper.SensorMapper;
import com.example.firesystem.model.Sensor;
import com.example.firesystem.repository.SensorRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    public List<SensorDto> getSensors() {
        return sensorRepository.findAll().stream()
                .map(SensorMapper::sensorToSensorDto)
                .toList();
    }

    public SensorDto getSensorById(Long id) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor with id " + id + " not found"));
        return SensorMapper.sensorToSensorDto(sensor);
    }

    @Transactional
    public Sensor createSensor(Sensor sensor) {
        return sensorRepository.save(sensor);
    }

    @Transactional
    public boolean deleteSensor(Long id) {
        if (sensorRepository.existsById(id)) {
            sensorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}