package com.example.firesystem.service;

import com.example.firesystem.dto.AlertDto;
import com.example.firesystem.dto.AlertRequestDto;
import com.example.firesystem.enums.StatusType;
import com.example.firesystem.exception.ResourceNotFoundException;
import com.example.firesystem.mapper.AlertMapper;
import com.example.firesystem.model.Alert;
import com.example.firesystem.repository.AlertRepository;
import com.example.firesystem.model.User;
import com.example.firesystem.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

        private final AlertRepository alertRepository;
        private final UserRepository userRepository;

        @Cacheable(value = "alerts", key = "'allAlerts'")
        public List<AlertDto> getAllAlerts() {
                log.info("Получение всех оповещений");
                List<AlertDto> alerts = alertRepository.findAll().stream()
                                .map(AlertMapper::alertToAlertDto)
                                .toList();
                log.debug("Найдено {} оповещений", alerts.size());
                return alerts;
        }

        @Cacheable(value = "alertsByStatus", key = "#status")
        public List<AlertDto> getAlertsByStatus(StatusType status) {
                log.info("Получение оповещений со статусом: {}", status);
                List<AlertDto> alerts = alertRepository.findByStatus(status).stream()
                                .map(AlertMapper::alertToAlertDto)
                                .toList();
                log.debug("Найдено {} оповещений со статусом {}", alerts.size(), status);
                return alerts;
        }

        @Cacheable(value = "alert", key = "#id")
        public AlertDto getAlertById(Long id) {
                log.info("Получение оповещения по ID: {}", id);
                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено", id);
                                        return new ResourceNotFoundException("Оповещение с ID " + id + " не найдено");
                                });
                log.debug("Оповещение с ID {} успешно найдено", id);
                return AlertMapper.alertToAlertDto(alert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alerts", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", allEntries = true)
        })
        @Transactional
        public AlertDto createAlert(AlertRequestDto alertRequestDto) {
                log.info("Создание нового оповещения для сенсора: {}", alertRequestDto.sensorId());

                Alert alert = new Alert();
                alert.setType(alertRequestDto.type());
                alert.setTimestamp(LocalDateTime.now());
                alert.setDescription(alertRequestDto.description());
                alert.setStatus(alertRequestDto.status() != null ? alertRequestDto.status() : StatusType.new_status);
                alert.setPhotoUrls(alertRequestDto.photoUrl());

                if (alertRequestDto.userId() != null) {
                        alert.setAssignedTo(userRepository.findById(alertRequestDto.userId())
                                        .orElseThrow(() -> {
                                                log.error("Пользователь с ID {} не найден при создании оповещения",
                                                                alertRequestDto.userId());
                                                return new ResourceNotFoundException("Пользователь с ID "
                                                                + alertRequestDto.userId()
                                                                + " не найден при создании оповещения");
                                        }));
                }

                Alert savedAlert = alertRepository.save(alert);
                log.info("Оповещение успешно создано с ID: {}", savedAlert.getId());
                return AlertMapper.alertToAlertDto(savedAlert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", allEntries = true)
        })
        @Transactional
        public AlertDto updateAlert(Long id, AlertRequestDto alertRequestDto) {
                log.info("Обновление оповещения с ID: {}", id);

                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено для обновления", id);
                                        return new ResourceNotFoundException(
                                                        "Оповещение с ID " + id + " не найдено для обновления");
                                });

                if (alertRequestDto.userId() != null) {
                        alert.setAssignedTo(userRepository.findById(alertRequestDto.userId())
                                        .orElseThrow(() -> {
                                                log.error("Пользователь с ID {} не найден при обновлении оповещения",
                                                                alertRequestDto.userId());
                                                return new ResourceNotFoundException("Пользователь с ID "
                                                                + alertRequestDto.userId()
                                                                + " не найден при обновлении оповещения");
                                        }));
                }

                alert.setType(alertRequestDto.type());
                alert.setDescription(alertRequestDto.description());
                alert.setStatus(alertRequestDto.status());
                alert.setPhotoUrls(alertRequestDto.photoUrl());

                Alert updatedAlert = alertRepository.save(alert);
                log.info("Оповещение с ID {} успешно обновлено", id);
                return AlertMapper.alertToAlertDto(updatedAlert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", allEntries = true)
        })
        @Transactional
        public void deleteAlert(Long id) {
                log.info("Удаление оповещения с ID: {}", id);

                if (!alertRepository.existsById(id)) {
                        log.error("Оповещение с ID {} не найдено для удаления", id);
                        throw new ResourceNotFoundException("Alert with id " + id + " not found");
                }

                alertRepository.deleteById(id);
                log.info("Оповещение с ID {} успешно удалено", id);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", allEntries = true)
        })
        @Transactional
        public AlertDto assignAlert(Long id, Long userId) {
                log.info("Назначение оповещения {} пользователю {}", id, userId);

                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено", id);
                                        return new ResourceNotFoundException("Оповещение с ID " + id + " не найдено");
                                });

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> {
                                        log.error("Пользователь с ID {} не найден", userId);
                                        return new ResourceNotFoundException(
                                                        "Пользователь с ID " + userId + " не найден");
                                });

                alert.setAssignedTo(user);
                Alert updatedAlert = alertRepository.save(alert);
                log.info("Оповещение {} успешно назначено пользователю {}", id, userId);
                return AlertMapper.alertToAlertDto(updatedAlert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", allEntries = true),
                        @CacheEvict(value = "alertsByStatus", key = "#status")
        })
        @Transactional
        public AlertDto changeStatus(Long id, StatusType status) {
                log.info("Изменение статуса оповещения {} на {}", id, status);

                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено", id);
                                        return new ResourceNotFoundException("Оповещение с ID " + id + " не найдено");
                                });

                alert.setStatus(status);
                Alert updatedAlert = alertRepository.save(alert);
                log.info("Статус оповещения {} успешно изменен на {}", id, status);
                return AlertMapper.alertToAlertDto(updatedAlert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true)
        })
        @Transactional
        public AlertDto addPhotoToAlert(Long id, String photoUrl) {
                log.info("Добавление фотографии к оповещению {}", id);

                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено", id);
                                        return new ResourceNotFoundException("Оповещение с ID " + id + " не найдено");
                                });

                alert.getPhotoUrls().add(photoUrl);
                Alert updatedAlert = alertRepository.save(alert);
                log.debug("Фотография добавлена к оповещению {}. Всего фотографий: {}",
                                id, updatedAlert.getPhotoUrls().size());
                return AlertMapper.alertToAlertDto(updatedAlert);
        }

        @Caching(evict = {
                        @CacheEvict(value = "alert", key = "#id"),
                        @CacheEvict(value = "alerts", allEntries = true)
        })
        @Transactional
        public AlertDto removePhotoFromAlert(Long id, String photoUrl) {
                log.info("Удаление фотографии из оповещения {}", id);

                Alert alert = alertRepository.findById(id)
                                .orElseThrow(() -> {
                                        log.error("Оповещение с ID {} не найдено", id);
                                        return new ResourceNotFoundException("Оповещение с ID " + id + " не найдено");
                                });

                boolean removed = alert.getPhotoUrls().remove(photoUrl);
                if (removed) {
                        Alert updatedAlert = alertRepository.save(alert);
                        log.debug("Фотография удалена из оповещения {}. Осталось фотографий: {}",
                                        id, updatedAlert.getPhotoUrls().size());
                        return AlertMapper.alertToAlertDto(updatedAlert);
                } else {
                        log.warn("Фотография не найдена в оповещении {}", id);
                        return AlertMapper.alertToAlertDto(alert);
                }
        }

        @Cacheable(value = "alertsBySensor", key = "#sensorId")
        public List<AlertDto> getAlertsBySensor(Long sensorId) {
                log.info("Поиск оповещений по сенсору: {}", sensorId);
                List<AlertDto> alerts = alertRepository.findBySensorId(sensorId).stream()
                                .map(AlertMapper::alertToAlertDto)
                                .toList();
                log.debug("Найдено {} оповещений для сенсора {}", alerts.size(), sensorId);
                return alerts;
        }

        @Transactional
        public Alert create(Alert alert) {
                log.debug("Сохранение оповещения через внутренний метод");
                return alertRepository.save(alert);
        }
}