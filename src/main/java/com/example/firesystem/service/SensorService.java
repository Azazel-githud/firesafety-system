package com.example.firesystem.service;

import com.example.firesystem.dto.SensorRequestDto;
import com.example.firesystem.dto.SensorResponseDto;
import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.mapper.SensorMapper;
import com.example.firesystem.model.Sensor;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.SensorRepository;
import com.example.firesystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "sensors", key = "'all'")
    public List<SensorResponseDto> getAllSensors() {
        log.info("Получение всех сенсоров");
        List<SensorResponseDto> sensors = sensorRepository.findAll().stream()
                .map(SensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
        log.debug("Найдено {} сенсоров", sensors.size());
        return sensors;
    }

    @Cacheable(value = "sensor", key = "#id")
    public SensorResponseDto getSensorById(Long id) {
        log.info("Получение сенсора по ID: {}", id);
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Сенсор с ID {} не найден", id);
                    return new ResourceNotFoundException("Sensor with id " + id + " not found");
                });
        log.debug("Сенсор с ID {} найден: модель={}, местоположение={}",
                id, sensor.getModel(), sensor.getLocation());
        return SensorMapper.sensorToSensorDto(sensor);
    }

    @Caching(evict = {
            @CacheEvict(value = "sensors", allEntries = true),
            @CacheEvict(value = "sensor", key = "#result.id()")
    })
    @Transactional
    public SensorResponseDto createSensor(SensorRequestDto sensorRequestDto) {
        log.info("Создание нового сенсора: модель={}, местоположение={}",
                sensorRequestDto.model(), sensorRequestDto.location());

        Sensor sensor = new Sensor();
        sensor.setModel(sensorRequestDto.model());
        sensor.setLocation(sensorRequestDto.location());

        if (sensorRequestDto.userId() != null) {
            User user = userRepository.findById(sensorRequestDto.userId())
                    .orElseThrow(() -> {
                        log.error("Пользователь с ID {} не найден при создании сенсора", sensorRequestDto.userId());
                        return new ResourceNotFoundException(
                                "User with id " + sensorRequestDto.userId() + " not found");
                    });
            sensor.setAssignedTo(user);
            log.debug("Сенсор назначен пользователю с ID: {}", sensorRequestDto.userId());
        }

        Sensor savedSensor = sensorRepository.save(sensor);
        log.info("Сенсор успешно создан с ID: {}", savedSensor.getId());
        return SensorMapper.sensorToSensorDto(savedSensor);
    }

    @Caching(evict = {
            @CacheEvict(value = "sensor", key = "#id"),
            @CacheEvict(value = "sensors", allEntries = true)
    })
    @Transactional
    public SensorResponseDto updateSensor(Long id, SensorRequestDto sensorRequestDto) {
        log.info("Обновление сенсора с ID: {}", id);

        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Сенсор с ID {} не найден для обновления", id);
                    return new ResourceNotFoundException("Sensor with id " + id + " not found");
                });

        log.debug("Обновление данных сенсора: модель с '{}' на '{}', местоположение с '{}' на '{}'",
                sensor.getModel(), sensorRequestDto.model(),
                sensor.getLocation(), sensorRequestDto.location());

        sensor.setModel(sensorRequestDto.model());
        sensor.setLocation(sensorRequestDto.location());

        if (sensorRequestDto.userId() != null) {
            User user = userRepository.findById(sensorRequestDto.userId())
                    .orElseThrow(() -> {
                        log.error("Пользователь с ID {} не найден при обновлении сенсора", sensorRequestDto.userId());
                        return new ResourceNotFoundException(
                                "User with id " + sensorRequestDto.userId() + " not found");
                    });
            sensor.setAssignedTo(user);
            log.debug("Сенсор назначен пользователю с ID: {}", sensorRequestDto.userId());
        } else {
            sensor.setAssignedTo(null);
            log.debug("Сенсор освобожден от назначения пользователю");
        }

        Sensor updatedSensor = sensorRepository.save(sensor);
        log.info("Сенсор с ID {} успешно обновлен", id);
        return SensorMapper.sensorToSensorDto(updatedSensor);
    }

    @Caching(evict = {
            @CacheEvict(value = "sensor", key = "#id"),
            @CacheEvict(value = "sensors", allEntries = true)
    })
    @Transactional
    public void deleteSensor(Long id) {
        log.info("Удаление сенсора с ID: {}", id);

        if (!sensorRepository.existsById(id)) {
            log.error("Сенсор с ID {} не найден для удаления", id);
            throw new ResourceNotFoundException("Sensor with id " + id + " not found");
        }

        sensorRepository.deleteById(id);
        log.info("Сенсор с ID {} успешно удален", id);
    }

    @Caching(evict = {
            @CacheEvict(value = "sensor", key = "#id"),
            @CacheEvict(value = "sensors", allEntries = true)
    })
    @Transactional
    public SensorResponseDto assignSensor(Long id, Long userId) {
        log.info("Назначение сенсора {} пользователю {}", id, userId);

        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Сенсор с ID {} не найден", id);
                    return new ResourceNotFoundException("Sensor with id " + id + " not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new ResourceNotFoundException("User with id " + userId + " not found");
                });

        sensor.setAssignedTo(user);
        Sensor updatedSensor = sensorRepository.save(sensor);
        log.info("Сенсор {} успешно назначен пользователю {}", id, userId);
        return SensorMapper.sensorToSensorDto(updatedSensor);
    }

    @Caching(evict = {
            @CacheEvict(value = "sensor", key = "#id"),
            @CacheEvict(value = "sensors", allEntries = true)
    })
    @Transactional
    public SensorResponseDto unassignSensor(Long id) {
        log.info("Освобождение сенсора {} от назначения", id);

        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Сенсор с ID {} не найден", id);
                    return new ResourceNotFoundException("Sensor with id " + id + " not found");
                });

        sensor.setAssignedTo(null);
        Sensor updatedSensor = sensorRepository.save(sensor);
        log.info("Сенсор {} успешно освобожден от назначения", id);
        return SensorMapper.sensorToSensorDto(updatedSensor);
    }
}